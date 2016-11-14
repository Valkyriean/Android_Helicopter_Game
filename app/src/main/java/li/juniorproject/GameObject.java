package li.juniorproject;

import android.graphics.Rect;

/**
 * Created by Li on 2016/5/20.
 */
public abstract class GameObject {
    //position of top left point
    protected int x;
    protected int y;
    //velocity in x and y direciton
    protected int dx;
    protected int dy;
    //the size of graph
    protected int width;
    protected int height;

//    public GameObject(){}
//
//    public GameObject(int x, int y){
//        this.x = x;
//        this.y = y;
//    }

    public void setX(int x){
        this.x = x;
    }

    public void setY(int y){
        this.y = y;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public int getHeight(){
        return height;
    }

    public int getWidth(){
        return width;
    }

    //pass the object as a rectangle to calculate the collision.
    public Rect getRectangle(){
        return new Rect(x , y, x+width ,y+height);
    }

}
