package com.swinestudios.unusualweapon;

import java.util.Random;

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
		
		public static Random random = new Random();
}
