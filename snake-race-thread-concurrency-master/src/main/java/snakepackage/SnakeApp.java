package snakepackage;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.*;

import enums.GridSize;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author jd-
 *
 */
public class SnakeApp {

    private static SnakeApp app;
    public static final int MAX_THREADS = 8;
    Snake[] snakes = new Snake[MAX_THREADS];
    private static final Cell[] spawn = {
        new Cell(1, (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell(GridSize.GRID_WIDTH - 2,
        3 * (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell(3 * (GridSize.GRID_WIDTH / 2) / 2, 1),
        new Cell((GridSize.GRID_WIDTH / 2) / 2, GridSize.GRID_HEIGHT - 2),
        new Cell(1, 3 * (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell(GridSize.GRID_WIDTH - 2, (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell((GridSize.GRID_WIDTH / 2) / 2, 1),
        new Cell(3 * (GridSize.GRID_WIDTH / 2) / 2,
        GridSize.GRID_HEIGHT - 2)};
    private static JFrame frame;
    private static Board board;
    private static int worstSnake;
    int nr_selected = 0;
    Thread[] thread = new Thread[MAX_THREADS];

    public SnakeApp() {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        frame = new JFrame("The Snake Race");
        //Cambios
        JButton startButton = new JButton("START");
        JButton pauseButton = new JButton("PAUSE");
        JButton resumeButton = new JButton("RESUME");
        actionListenerBuilder(startButton,pauseButton,resumeButton);
        //
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // frame.setSize(618, 640);
        frame.setSize(GridSize.GRID_WIDTH * GridSize.WIDTH_BOX + 17,
                GridSize.GRID_HEIGHT * GridSize.HEIGH_BOX + 40);
        frame.setLocation(dimension.width / 2 - frame.getWidth() / 2,
                dimension.height / 2 - frame.getHeight() / 2);
        board = new Board();
        
        
        frame.add(board,BorderLayout.CENTER);
        
        JPanel actionsBPabel=new JPanel();
        actionsBPabel.setLayout(new FlowLayout());
        actionsBPabel.add(startButton);
        actionsBPabel.add(pauseButton);
        actionsBPabel.add(resumeButton);
        frame.add(actionsBPabel,BorderLayout.SOUTH);

        worstSnake = -1;

    }

    public static void main(String[] args) {
        app = new SnakeApp();
        app.init();
    }

    private void init() {
        
        
        
        for (int i = 0; i != MAX_THREADS; i++) {
            
            snakes[i] = new Snake(i + 1, spawn[i], i + 1);
            snakes[i].addObserver(board);
            thread[i] = new Thread(snakes[i]);
            thread[i].start();
        }

        frame.setVisible(true);

            
        while (true) {
            int x = 0;
            for (int i = 0; i != MAX_THREADS; i++) {
                if (snakes[i].isSnakeEnd() == true) {
                    if(x==0){
                        worstSnake = i;   //Save the first snake that end
                    }
                    x++;
                }
            }
            if (x == MAX_THREADS) {
                break;
            }
        }


        System.out.println("Thread (snake) status:");
        for (int i = 0; i != MAX_THREADS; i++) {
            System.out.println("["+i+"] :"+thread[i].getState());
        }
        

    }

    public static SnakeApp getApp() {
        return app;
    }

    /**
     * Aqui se generan los action Listeners de los botones
     * @param b1 Start
     * @param b2 Pause
     * @param b3 Resume
     */
    public static void actionListenerBuilder(JButton start, JButton pause, JButton resume){
        //Pause Button
        pause.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO
                 int largestSnake = findLargestSnake();
                 int size = app.snakes[largestSnake].getGrowing()+1;
                 String deadSnake = (worstSnake<0)?"No one ends yet":Integer.toString(worstSnake+1);
                 String message = "- Largest snake: " + largestSnake + ", size: " + size + "\n"
                                    + "- Worst snake: " + deadSnake;
                 int option = JOptionPane.showOptionDialog(frame,message,"Serpiente mas larga",JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE,null,new String[]{"Volver al juego"},"Back to the Game");
                 // option  0 es volver al juego
            }
        });
    }

    /**
     * This will check the largest and the
     * @return
     */
    public static int findLargestSnake(){
        int size = -1;
        int snake = -1;
        for(int i=0; i != MAX_THREADS; i++){
            if((!app.snakes[i].isSnakeEnd()) && (app.snakes[i].getGrowing()+1 > size)){
                size = app.snakes[i].getGrowing()+1;
                snake = i;                  //If the snake is larger than the actual parameter, update the parameter(size) and store the snake(i)
            }
        }
        return snake;
        // HashMap<Integer,Integer> tuple = new HashMap<>();
        // int sizeOfLongest = -7;
        // int idtOfLongest = 0;
        // int operation = 0;
        // for (Snake snake : app.snakes) {
        //     if (!snake.isSnakeEnd()) {
        //         operation = snake.getGrowing() - snake.getINIT_SIZE();
        //         if (operation > sizeOfLongest) {
        //             sizeOfLongest = operation;
        //             idtOfLongest = snake.getIdt();
        //         }
        //     }
        // }
        // tuple.put(idtOfLongest,sizeOfLongest);
        // return tuple;
    }
}
