package snakepackage;

import java.util.LinkedList;
import java.util.Observable;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;

import enums.Direction;
import enums.GridSize;

public class Snake extends Observable implements Runnable {

    private int idt;
    private Cell head;
    private Cell newCell;
    private ConcurrentLinkedDeque<Cell> snakeBody = new ConcurrentLinkedDeque<Cell>();
    //private Cell objective = null;
    private Cell start = null;

    private boolean snakeEnd = false;

    private int direction = Direction.NO_DIRECTION;
    private final int INIT_SIZE = 3;

    private boolean hasTurbo = false;

    private boolean stopped = false;
    private int jumps = 0;
    private boolean isSelected = false;
    private int growing = 0;
    public boolean goal = false;
    private int size;
    private boolean isRunning = true;
    private Object lock;

    private boolean paused = false;


    public Snake(int idt, Cell head, int direction, Object lock) {
        this.idt = idt;
        this.direction = direction;
        this.isRunning = false;
        this.lock = lock;
        generateSnake(head);

    }

    public boolean isStopped() {
        return stopped;
    }

    public void stopSnake() {
        stopped = true;
    }

    public void pauseSnake() {
        stopped = true;
    }

    public void resumeSnake() {
        stopped = false;
        synchronized (lock) {
            lock.notify();
        }
    }

    public boolean isSnakeEnd() {
        return snakeEnd;
    }

    private void generateSnake(Cell head) {
        start = head;
        //Board.gameboard[head.getX()][head.getY()].reserveCell(jumps, idt);
        snakeBody.add(head);
        growing = INIT_SIZE - 1;
        size = INIT_SIZE;
    }

    @Override
    public void run() {
        while (!snakeEnd) {
            if(!stopped) {
                snakeCalc();

                //NOTIFY CHANGES TO GUI
                setChanged();
                notifyObservers();

                try {
                    if (hasTurbo == true) {
                        Thread.sleep(500 / 3);
                    } else {
                        Thread.sleep(500);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else {
                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        fixDirection(head);
        

    }

    private void snakeCalc() {
        head = snakeBody.peekFirst();

        newCell = head;

        newCell = changeDirection(newCell);
        
        randomMovement(newCell);

        checkIfFood(newCell);
        checkIfJumpPad(newCell);
        checkIfTurboBoost(newCell);
        checkIfBarrier(newCell);
        
        snakeBody.push(newCell);

        if (growing <= 0) {
            newCell = snakeBody.peekLast();
            snakeBody.remove(snakeBody.peekLast());
            Board.synchronizedGameBoard.get(newCell.getX())
                                            .get(newCell.getY()).freeCell();
        } else if (growing != 0) {
            growing--;
        }

    }

    private void checkIfBarrier(Cell newCell) {
        if (Board.synchronizedGameBoard.get(newCell.getX())
                                            .get(newCell.getY()).isBarrier()) {
            // crash
            System.out.println("[" + idt + "] " + "CRASHED AGAINST BARRIER "
                    + newCell.toString());
            snakeEnd=true;
        }
    }

    
    private Cell fixDirection(Cell newCell) {

        // revert movement
        if (direction == Direction.LEFT
                && head.getX() + 1 < GridSize.GRID_WIDTH) {
            newCell = Board.synchronizedGameBoard.get(head.getX() + 1)
                                                    .get(head.getY());
        } else if (direction == Direction.RIGHT && head.getX() - 1 >= 0) {
            newCell = Board.synchronizedGameBoard.get(head.getX() - 1)
                                                    .get(head.getY());
        } else if (direction == Direction.UP
                && head.getY() + 1 < GridSize.GRID_HEIGHT) {
            newCell = Board.synchronizedGameBoard.get(head.getX())
                                                    .get(head.getY() + 1);
        } else if (direction == Direction.DOWN && head.getY() - 1 >= 0) {
            newCell = Board.synchronizedGameBoard.get(head.getX())
                                                    .get(head.getY() - 1);
        }

        randomMovement(newCell);
        return newCell;
    }

    private boolean checkIfOwnBody(Cell newCell) {
        for (Cell c : snakeBody) {
            if (newCell.getX() == c.getX() && newCell.getY() == c.getY()) {
                return true;
            }
        }
        return false;

    }

    private void randomMovement(Cell newCell) {
        Random random = new Random();
        int tmp = random.nextInt(4) + 1;
        if (tmp == Direction.LEFT && !(direction == Direction.RIGHT)) {
            direction = tmp;
        } else if (tmp == Direction.UP && !(direction == Direction.DOWN)) {
            direction = tmp;
        } else if (tmp == Direction.DOWN && !(direction == Direction.UP)) {
            direction = tmp;
        } else if (tmp == Direction.RIGHT && !(direction == Direction.LEFT)) {
            direction = tmp;
        }
    }

    private void checkIfTurboBoost(Cell newCell) {
        if (Board.synchronizedGameBoard.get(newCell.getX())
                                        .get(newCell.getY()).isTurbo_boost()) {
            // get turbo_boost
            for (int i = 0; i != Board.NR_TURBO_BOOSTS; i++) {
                if (Board.turbo_boosts[i] == newCell) {
                    Board.turbo_boosts[i].setTurbo_boost(false);
                    Board.turbo_boosts[i] = new Cell(-5, -5);
                    hasTurbo = true;
                }

            }
            System.out.println("[" + idt + "] " + "GETTING TURBO BOOST "
                    + newCell.toString());
        }
    }

    private void checkIfJumpPad(Cell newCell) {

        if (Board.synchronizedGameBoard.get(newCell.getX())
                                        .get(newCell.getY()).isJump_pad()) {
            // get jump_pad
            for (int i = 0; i != Board.NR_JUMP_PADS; i++) {
                if (Board.jump_pads[i] == newCell) {
                    Board.jump_pads[i].setJump_pad(false);
                    Board.jump_pads[i] = new Cell(-5, -5);
                    this.jumps++;
                }

            }
            System.out.println("[" + idt + "] " + "GETTING JUMP PAD "
                    + newCell.toString());
        }
    }

    public int getGrowing() {
        return growing;
    }

    public int getINIT_SIZE() {
        return INIT_SIZE;
    }

    public int getSize(){
        return size;
    }

    private void checkIfFood(Cell newCell) {
        Random random = new Random();

        if (Board.synchronizedGameBoard.get(newCell.getX())
                                        .get(newCell.getY()).isFood()) {
            // eat food
            growing += 3;
            size +=3;
            int x = random.nextInt(GridSize.GRID_HEIGHT);
            int y = random.nextInt(GridSize.GRID_WIDTH);

            System.out.println("[" + idt + "] " + "EATING "
                    + newCell.toString());

            for (int i = 0; i != Board.NR_FOOD; i++) {
                if (Board.food[i].getX() == newCell.getX()
                        && Board.food[i].getY() == newCell.getY()) {
                    Board.synchronizedGameBoard.get(Board.food[i].getX())
                                                .get(Board.food[i].getY())
                                                    .setFood(false);

                    while (Board.synchronizedGameBoard.get(x).get(y).hasElements()) {
                        x = random.nextInt(GridSize.GRID_HEIGHT);
                        y = random.nextInt(GridSize.GRID_WIDTH);
                    }
                    Board.food[i] = new Cell(x, y);
                    Board.synchronizedGameBoard.get(x).get(y).setFood(true);
                }
            }
        }

    }

    private Cell changeDirection(Cell newCell) {
        // Avoid out of bounds

        while (direction == Direction.UP && (newCell.getY() - 1) < 0) {
            if ((head.getX() - 1) < 0) {
                this.direction = Direction.RIGHT;
            } else if ((head.getX() + 1) == GridSize.GRID_WIDTH) {
                this.direction = Direction.LEFT;
            } else {
                randomMovement(newCell);
            }
        }
        while (direction == Direction.DOWN
                && (head.getY() + 1) == GridSize.GRID_HEIGHT) {
            if ((head.getX() - 1) < 0) {
                this.direction = Direction.RIGHT;
            } else if ((head.getX() + 1) == GridSize.GRID_WIDTH) {
                this.direction = Direction.LEFT;
            } else {
                randomMovement(newCell);
            }
        }
        while (direction == Direction.LEFT && (head.getX() - 1) < 0) {
            if ((newCell.getY() - 1) < 0) {
                this.direction = Direction.DOWN;
            } else if ((head.getY() + 1) == GridSize.GRID_HEIGHT) {
                this.direction = Direction.UP;
            } else {
                randomMovement(newCell);
            }
        }
        while (direction == Direction.RIGHT
                && (head.getX() + 1) == GridSize.GRID_WIDTH) {
            if ((newCell.getY() - 1) < 0) {
                this.direction = Direction.DOWN;
            } else if ((head.getY() + 1) == GridSize.GRID_HEIGHT) {
                this.direction = Direction.UP;
            } else {
                randomMovement(newCell);
            }
        }

        switch (direction) {
            case Direction.UP:
                newCell = Board.synchronizedGameBoard.get(head.getX()).get(head.getY() - 1);
                break;
            case Direction.DOWN:
                newCell = Board.synchronizedGameBoard.get(head.getX()).get(head.getY() + 1);
                break;
            case Direction.LEFT:
                newCell = Board.synchronizedGameBoard.get(head.getX() - 1).get(head.getY());
                break;
            case Direction.RIGHT:
                newCell = Board.synchronizedGameBoard.get(head.getX() + 1).get(head.getY());
                break;
        }
        return newCell;
    }

    public void searchObjective(Cell objective) {

        Random random = new Random();

        // MOVE DIRECTLY TO OBJECTIVE
        if (direction == Direction.LEFT || direction == Direction.RIGHT) {
            if (direction == Direction.LEFT) {
                if (head.getY() > objective.getY()) {
                    direction = Direction.UP;
                } else if (head.getY() < objective.getY()) {
                    direction = Direction.DOWN;
                } else if (head.getY() == objective.getY()
                        && head.getX() < objective.getX()) {
                    direction = random.nextInt(2) + 3;
                }
            } else if (direction == Direction.RIGHT) {
                if (head.getY() > objective.getY()) {
                    direction = Direction.UP;
                } else if (head.getY() < objective.getY()) {
                    direction = Direction.DOWN;
                } else if (head.getY() == objective.getY()
                        && head.getX() > objective.getX()) {
                    direction = random.nextInt(2) + 3;
                }
            }
        } else if (direction == Direction.UP || direction == Direction.DOWN) {
            if (direction == Direction.UP) {
                if (head.getX() > objective.getX()) {
                    direction = Direction.LEFT;
                } else if (head.getX() < objective.getX()) {
                    direction = Direction.RIGHT;
                } else if (head.getX() == objective.getX()
                        && head.getY() < objective.getY()) {
                    direction = random.nextInt(2) + 1;
                }
            } else if (direction == Direction.DOWN) {
                if (head.getX() > objective.getX()) {
                    direction = Direction.LEFT;
                } else if (head.getX() < objective.getX()) {
                    direction = Direction.RIGHT;
                } else if (head.getX() == objective.getX()
                        && head.getY() > objective.getY()) {
                    direction = random.nextInt(2) + 1;
                }
            }
        }
    }

    /*public void setObjective(Cell c) {
        System.out.println("Setting objective - " + c.getX() + ":" + c.getY()
                + " for Snake" + this.idt);
        this.objective = c;
    }*/

    public ConcurrentLinkedDeque<Cell> getBody() {
        return this.snakeBody;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public int getIdt() {
        return idt;
    }

}
