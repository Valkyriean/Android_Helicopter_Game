package li.juniorproject;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by Li on 6/6/2016.
 */
public class Coin extends GameObject {
    private Animation animation = new Animation();
    private Bitmap spritesheet;


    public Coin(Bitmap res, int x, int y, int w, int h, int numberFrames){
        spritesheet = res;
        super.x=x;
        super.y = y;
        width = w;
        height = h;

        Bitmap[] image = new Bitmap[numberFrames];

        for(int i = 0; i< image.length; i++){
            image[i] = Bitmap.createBitmap(spritesheet,i*width,0,width,height);
        }


        animation.setFrames(image);
        animation.setDelay(50);
    }

    public void update(){
        x += GamePanel.MOVESPEED;
        animation.update();
    }

    public void draw(Canvas canvas){
        try{
            canvas.drawBitmap(animation.getImage(),x,y,null);
        }catch(Exception e){}
    }
}
