package com.swinestudios.unusualweapon;

import org.mini2Dx.core.geom.Rectangle;

public class CaveSystem{
	
	public int[][] terrain = { {1, 0, 0, 1, 0}, //temporary
			{1, 1, 0, 0, 0},
			{1, 1, 0, 0, 0},
			{1, 0, 0, 0, 1},
			{1, 0, 0, 1, 1}};;
	
	public float x, y; //the top-left corner of the entire cave system
	public final int tileSize = 32;
	
	public Gameplay level;
	
	public CaveSystem(float x, float y, Gameplay level){
		this.x = x;
		this.y = y;
		this.level = level;
		//terrain = new int[5][5]; //TODO temporary size
	}
	
	/*
	 * TODO Generates the terrain and saves it in the 2D array.
	 * 
	 * 1 = wall
	 * 0 = empty
	 */
	public void generateTerrain(){
		
	}
	
	/*
	 * Adds the generated terrain to the level.
	 */
	public void addTerrain(){
		for(int i = 0; i < terrain.length; i++){
			for(int j = 0; j < terrain[i].length; j++){
				if(terrain[i][j] == 1){ //if a solid
					level.solids.add(new Rectangle(x + j * tileSize, y + i * tileSize, tileSize, tileSize));
				}
			}
		}
	}

}
