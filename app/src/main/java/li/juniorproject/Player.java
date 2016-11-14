package li.juniorproject;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by Li on 2016/5/20.
 */
public class Player extends GameObject {
    //import the game res png but not really using here
    private Bitmap spritesheet;
    private int score;
    //is accelerating upward
    private boolean up;
    private boolean playing;
    //put game png here
    private Animation animation = new Animation();
    private long startTime;

    public Player(Bitmap res, int w, int h ,int numFrames){

        //开始玩家高度
        //start location (x location will not changing)
        x = 100;
        //y at the middle
        y = GamePanel.HEIGHT/2;
        //开始的加速度
        dy = 0;
        score = 0;
        height = h;
        width = w;

        Bitmap[] image = new Bitmap[numFrames];
        spritesheet = res;

        for(int i = 0; i < image.length; i++){
            image[i] = Bitmap.createBitmap(spritesheet,i*width,0,width,height);

        }

        animation.setFrames(image);
        animation.setDelay(10);
        startTime = System.nanoTime();
    }

    //read if player is going up
    public void setUp(boolean b){
        up = b;
    }

    public void update(){
        long elapsed = (System.nanoTime()-startTime/1000000);

        //分数100毫秒+1一次,后去除开始时间
        //add score by one after 100 nano second
        if(elapsed > 100) {
            score++;
            startTime = System.nanoTime();

        }

        animation.update();

        //when up, change
        if(up) {
            dy -= 1;

        }else{
            dy += 1;
        }

        if(dy>14)
            dy = 14;
        if(dy<-14)
            dy = -14;

        y +=dy*2;
    }

    public void draw(Canvas canvas){
        canvas.drawBitmap(animation.getImage(),x,y,null);
    }

    public int getScore(){
        return score;
    }

    public boolean getPlaying(){
        return playing;
    }
    public void setPlaying(boolean b){
        playing = b;
    }

    public void resetDY(){
        dy = 0;
    }
    public void resetScore(){
        score = 0;
    }

    public void getCoin() {score+=20;}
}
