package com.swinestudios.unusualweapon;

import java.util.Random;

import org.mini2Dx.core.graphics.Animation;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class Global_Constants {

		public static float gravity = 1.0f;//purely placeholder values for now
		
		public static float minZero( float input ){
			return( input > 0 ? input : 0 );
		}
		
		public static float unitVectorX(float x, float y){
			float c = (float) Math.pow( Math.pow(x, 2) + Math.pow(y, 2), 0.5 );
			
			if(c == 0){
				return 0;
			}
			
			
			return (float) ( x / c);
		}
		
		public static float unitVectorY(float x, float y){
			float c = (float) Math.pow( Math.pow(x, 2) + Math.pow(y, 2), 0.5 );
			
			if(c == 0){
				return 0;
			}
			
			
			return (float) ( y / c);
		}
		
		/*
		 * Flips images to their right orientation
		 */
		public static void adjustSprite(Sprite... s){
			for(int i = 0; i < s.length; i++){
				s[i].setOrigin(0, 0);
				s[i].flip(false, true);
			}
		}
		
		/*
		 * Changes the size of all given frames
		 */
		public static void setFrameSizes(Animation<Sprite> a, float width, float height){
			for(int i = 0; i < a.getNumberOfFrames(); i++){
				a.getFrame(i).setSize(width, height);
			}
		}
		
		public static Random random = new Random();
}
