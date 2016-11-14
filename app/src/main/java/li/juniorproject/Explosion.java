package li.juniorproject;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by AlexanderLI on 5/27/16.
 */
public class Explosion {
    private int x;
    private int y;
    private int width;
    private int height;
    private int row;
    private Animation animation = new Animation();
    private Bitmap spritesheet;
    private boolean moving;
    public boolean done;

    public Explosion (Bitmap res, int x, int y, int w, int h, int numFrames, boolean moving){
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
        this.moving = moving;
        done = false;

        Bitmap[] image = new Bitmap[numFrames];

        spritesheet = res;

        for(int i = 0; i<image.length; i++){
            if(i%5==0 && i >0) {
                row++;
            }
            image[i] = Bitmap.createBitmap(spritesheet, (i-(5*row))*width, row*height, width, height);
        }
        animation.setFrames(image);
        animation.setDelay(10);
    }

    public void draw(Canvas canvas){
        if(!animation.playedOnce()){
            canvas.drawBitmap(animation.getImage(), x, y, null);
        }
    }

    public void update(){
        if(!animation.playedOnce()){
            if(moving){
                x += GamePanel.MOVESPEED;
            }
            animation.update();

        }else{
            done = true;
        }
    }

    public int getHeight(){
        return height;
    }

}
