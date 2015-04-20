package com.swinestudios.unusualweapon;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.mini2Dx.core.geom.Rectangle;

public class CaveSystem{

	public int[][] terrain;
	public int[][] tileTypes;

	public float x, y; //the top-left corner of the entire cave system
	public static final int tileSize = 32; 

	public Gameplay level;
	public Random random;

	public ArrayList<Miner> miners;
	public Miner miner1;

	public final int maxSteps = 500; //How many iterations each miner will carry out
	public final int trimSteps = 5; //How many iterations of removing idle blocks
	public final int spawnChance = 10; //The percent probability of another miner spawning
	public final int minimumNeighborsNeeded = 4; //The minimum # of neighbors needed for a solid to be removed

	public CaveSystem(float x, float y, Gameplay level){
		this.x = x;
		this.y = y;
		this.level = level;
		terrain = new int[100][100]; //TODO temporary size
		tileTypes = new int[terrain.length][terrain[0].length];
		miners = new ArrayList<Miner>();
		miner1 = new Miner(terrain[0].length / 2, terrain.length / 2);
		miners.add(miner1);
		random = new Random();
	}

	/*
	 * TODO Generates the terrain and saves it in the 2D array.
	 * 
	 * 1 = wall
	 * 0 = empty
	 */
	public void generateTerrain(){
		for(int[] row : terrain){ //make everything solid
			Arrays.fill(row, 1);
		}

		for(int n = 0; n < maxSteps; n++){
			for(int i = 0; i < miners.size(); i++){
				if(miners.get(i).isActive){
					miners.get(i).moveMiner();
				}
			}
			int spawner = random.nextInt(100) + 1;
			if(spawner <= spawnChance){
				//Connected generation
				/*ArrayList<Point> temp = findEmptySpace();
				int tempChoice;
				if(temp.size() > 0){
					tempChoice = random.nextInt(temp.size());
					miners.add(new Miner((int)temp.get(tempChoice).getX(), (int)temp.get(tempChoice).getY()));
				}
				else{
					miners.add(new Miner(terrain[0].length / 2, terrain.length / 2));
				}*/

				//Disconnected generation
				miners.add(new Miner(random.nextInt(terrain[0].length), random.nextInt(terrain.length))); 
			}
		}
		miners.clear();

		for(int i = 0; i < trimSteps; i++){
			trimTerrain();
		}

		//Box in the terrain
		for(int i = 0; i < terrain.length; i++){
			for(int j = 0; j < terrain[i].length; j++){
				if(i == 0 || i == terrain.length - 1 || j == 0 || j == terrain[i].length -1){
					terrain[i][j] = 1;
				}
			}
		}

		//Initialize tile types
		for(int i = 0; i < terrain.length; i++){
			for(int j = 0; j < terrain[i].length; j++){
				if(terrain[i][j] == 1){
					tileTypes[i][j] = getTileType(j, i);
				}
			}
		}
	}
	
	/*
	 * TODO Generates terrain using cellular automata
	 */
	public void generateTerrain2(){
		//Fill terrain with 45% probability
		for(int i = 0; i < terrain.length; i++){
			for(int j = 0; j < terrain[i].length; j++){
				if(random.nextInt(100) <= 45){
					terrain[i][j] = 1;
				}
			}
		}
		for(int n = 0; n < 4; n++){
			for(int i = 0; i < terrain.length; i++){
				for(int j = 0; j < terrain[i].length; j++){
					System.out.println(getNeighborCount(j, i, 1));
					if(getNeighborCount(j, i, 1) >= 4 || getNeighborCount(j, i, 2) <= 2){
						terrain[i][j] = 1;
					}
					else{
						terrain[i][j] = 0;
					}
				}
			}
		}
		for(int n = 0; n < 3; n++){
			for(int i = 0; i < terrain.length; i++){
				for(int j = 0; j < terrain[i].length; j++){
					if(getNeighborCount(j, i, 1) >= 4){
						terrain[i][j] = 1;
					}
					else{
						terrain[i][j] = 0;
					}
				}
			}
		}
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

	/*
	 * Adds the generated terrain to the level with some optimization.
	 */
	public void addOptimizedTerrain(){
		for(int i = 0; i < terrain.length; i++){
			int currentRun = 0;
			int startRunX = 0;
			int startRunY = 0;
			for(int j = 0; j < terrain[i].length; j++){
				if(terrain[i][j] == 1){ //if a solid
					if(currentRun == 0){ //if starting a new run
						startRunX = j;
						startRunY = i;
					}
					currentRun++;
				}
				else{ //the current run of solids is done
					level.solids.add(new Rectangle(x + startRunX * tileSize, y + startRunY * tileSize, currentRun * tileSize, tileSize));
					currentRun = 0;
				}
				if(j == terrain[i].length - 1){ //if on the right edge of the terrain
					level.solids.add(new Rectangle(x + startRunX * tileSize, y + startRunY * tileSize, currentRun * tileSize, tileSize));
					currentRun = 0;
				}
			}
		}
	}

	/*
	 * Removes idle blocks of terrain
	 */
	public void trimTerrain(){
		for(int y = 0; y < terrain.length; y++){
			for(int x = 0; x < terrain[y].length; x++){
				int counter = 0;
				int leftX = Math.abs((x - 1) % terrain[0].length);
				int rightX = Math.abs((x + 1) % terrain[0].length);
				int upY = Math.abs((y - 1) % terrain.length);
				int downY = Math.abs((y + 1) % terrain.length);

				//count how many neighbors there are
				counter += Math.signum(terrain[y][leftX]);
				counter += Math.signum(terrain[y][rightX]);
				counter += Math.signum(terrain[upY][x]);
				counter += Math.signum(terrain[downY][x]);
				counter += Math.signum(terrain[upY][leftX]);
				counter += Math.signum(terrain[upY][rightX]);
				counter += Math.signum(terrain[downY][leftX]);
				counter += Math.signum(terrain[downY][rightX]);

				if(counter < minimumNeighborsNeeded){
					terrain[y][x] = 0;
				}
			}
		}
	}
	
	/*
	 * Returns the number of solids within n steps
	 */
	public int getNeighborCount(int row, int col, int n){
		if(n <= 0){
			return 0;
		}
		if(row == 0 || col == 0 || row == terrain.length - 1 || col == terrain[0].length - 1){
			return 1;
		}
		else{
			return terrain[row][col] + getNeighborCount(row+1, col, n--) + getNeighborCount(row-1, col, n--) + getNeighborCount(row, col+1, n--) + getNeighborCount(row, col-1, n--);
		}
	}

	/*
	 * Returns a list of all coordinates in the terrain that are the center of a 3 x 3 empty square.
	 */
	public ArrayList<Point> findEmptySpace(){
		ArrayList<Point> list = new ArrayList<Point>();
		for(int i = 2; i < terrain.length - 2; i++){
			for(int j = 2; j < terrain[i].length - 2; j++){
				if( terrain[i][j] + 
						terrain[i][j+1] + 
						terrain[i][j-1] + 
						terrain[i+1][j] + 
						terrain[i-1][j] +
						terrain[i+1][j+1] +
						terrain[i+1][j-1] +
						terrain[i-1][j+1] +
						terrain[i-1][j-1] == 0){
					list.add(new Point(j, i));
				}
			}
		}
		return list;
	}

	class Miner{

		public int x, y;

		public int choice;

		public Random random;

		public boolean isActive;

		public Miner(int x, int y){
			this.x = x;
			this.y = y;
			isActive = true;
			random = new Random();
			choice = random.nextInt(4) + 1;
		}
		
		/*
		 * Returns the distance between this miner and the given miner.
		 */
		public float distanceTo(Miner target){
			return ((float)Math.pow(Math.pow((target.y - this.y), 2.0) + Math.pow((target.x - this.x), 2.0), 0.5));
		}
		
		public Miner findClosestMiner(){
			Miner temp = this;
			float maxDist = 0;
			for(int i = 0; i < miners.size(); i++){
				if(distanceTo(miners.get(i)) > maxDist){
					maxDist = distanceTo(miners.get(i));
					temp = miners.get(i);
				}
			}
			return temp;
		}

		public void moveMiner(){
			if(choice == 1){ //north
				y--;
			}
			else if(choice == 2){ //east
				x++;
			}
			else if(choice == 3){ //south
				y++;
			}
			else if(choice == 4){ //west
				x--;
			}

			//make sure the miner's position is within the terrain
			x = Math.abs(x % terrain[0].length);
			y = Math.abs(y % terrain.length);

			choice = random.nextInt(4) + 1;

			/*
			//Pull miners together TODO not sure if needed
			//Miner other = miners.get(random.nextInt(miners.size()));
			Miner other = findClosestMiner();
			if(other != this){
				if(other.x > this.x){ //if another miner is to the right
					if(other.y > this.y){ //if another miner is below
						boolean temp = random.nextBoolean();
						choice = temp ? 1 : 4; //move north or west
					}
					else if(other.y <= this.y){ //if another miner is above
						boolean temp = random.nextBoolean();
						choice = temp ? 3 : 4; //move west or south
					}
				}
				else if(other.x <= this.x){ //if another miner is to the left
					if(other.y > this.y){ //if another miner is below
						boolean temp = random.nextBoolean();
						choice = temp ? 1 : 2; //move north or east
					}
					else if(other.y <= this.y){ //if another miner is above
						boolean temp = random.nextBoolean();
						choice = temp ? 2 : 3; //move east or south
					}
				}
				choice = (choice + 2) % 4;
			}*/

			//"Mine" the solid
			if(terrain[y][x] == 1){
				terrain[y][x] = 0;
			}

			//Remove any miners that hit an edge
			if(x == 0 || y == 0 || x == terrain[0].length - 1 || y == terrain.length - 1){
				isActive = false;
				miners.remove(this);
			}

			/* TODO not sure if needed or not
			int leftX = Math.abs((x - 1) % terrain[0].length);
			int rightX = Math.abs((x + 1) % terrain[0].length);
			int upY = Math.abs((y - 1) % terrain.length);
			int downY = Math.abs((y + 1) % terrain.length);

			//Stop this miner if it is not bordering a wall 
			if(	   terrain[y][leftX] != 1 
				&& terrain[y][rightX] != 1 
				&& terrain[downY][x] != 1 
				&& terrain[upY][x] != 1 
				&& terrain[upY][leftX] != 1 
				&& terrain[upY][rightX] != 1
				&& terrain[downY][leftX] != 1 
				&& terrain[downY][rightX] != 1){
				isActive = false;
				miners.remove(this);
			}*/
		}

	}

	/*
	 * Returns the type of "tile" a given coordinate is. Used for auto-tiling.
	 * 
	 * Credit to Nocturne, modifications by ShaunJS, further modified by pigrocket and TheBlueGamer55
	 */
	public int getTileType(int x, int y){
		int tile;

		boolean w_left, w_right, w_up, w_down, w_upleft, w_downleft, w_upright, w_downright;

		int iw = 1; 

		if(x - iw < 0 && y - iw < 0){ //top-left corner
			w_left = true; w_upleft = true; w_downleft = true; w_up = true; w_upright = true;
			w_right = terrain[y][x+iw] == 1; 
			w_downright = terrain[y+iw][x+iw] == 1;
			w_down = terrain[y+iw][x] == 1; 
		}
		else if(x - iw < 0 && y + iw > terrain.length - 1){ //bottom-left corner
			w_left = true; w_upleft = true; w_downleft = true; w_down = true; w_downright = true;
			w_up = terrain[y-iw][x] == 1;
			w_upright = terrain[y-iw][x+iw] == 1; 
			w_right = terrain[y][x+iw] == 1; 
		}
		else if(x + iw > terrain[0].length - 1 && y - iw < 0){ //top-right corner
			w_right = true; w_upright = true; w_downright = true; w_up = true; w_upleft = true;
			w_left = terrain[y][x-iw] == 1;
			w_downleft = terrain[y+iw][x-iw] == 1;
			w_down = terrain[y+iw][x] == 1; 
		}
		else if(x + iw > terrain[0].length - 1 && y + iw > terrain.length - 1){ //bottom-right corner
			w_down = true; w_downleft = true; w_downright = true; w_right = true; w_upright = true;
			w_up = terrain[y-iw][x] == 1; 
			w_upleft = terrain[y-iw][x-iw] == 1; 
			w_left = terrain[y][x-iw] == 1;
		}

		else if (x-iw < 0) { //left edge
			w_left = true; w_upleft = true; w_downleft = true;
			w_up = terrain[y-iw][x] == 1;
			w_upright = terrain[y-iw][x+iw] == 1; 
			w_right = terrain[y][x+iw] == 1; 
			w_downright = terrain[y+iw][x+iw] == 1; 
			w_down = terrain[y+iw][x] == 1;
		} 
		else if (x+iw > terrain[0].length - 1) { //right edge
			w_right = true; w_upright = true; w_downright = true;
			w_up = terrain[y-iw][x] == 1;
			w_upleft = terrain[y-iw][x-iw] == 1; 
			w_left = terrain[y][x-iw] == 1;
			w_downleft = terrain[y+iw][x-iw] == 1;
			w_down = terrain[y+iw][x] == 1;
		} 
		else if (y-iw < 0) { //top edge
			w_up = true; w_upright = true; w_upleft = true;
			w_left = terrain[y][x-iw] == 1;
			w_downleft = terrain[y+iw][x-iw] == 1;
			w_down = terrain[y+iw][x] == 1; 
			w_right = terrain[y][x+iw] == 1; 
			w_downright = terrain[y+iw][x+iw] == 1;
		}
		else if (y+iw > terrain.length - 1) { //bottom edge
			w_down = true; w_downright = true; w_downleft = true;
			w_left = terrain[y][x-iw] == 1;
			w_up = terrain[y-iw][x] == 1;
			w_upleft = terrain[y-iw][x-iw] == 1;
			w_upright = terrain[y-iw][x+iw] == 1; 
			w_right = terrain[y][x+iw] == 1; 
		} 
		else{ //in the middle
			w_left = terrain[y][x-iw] == 1; 
			w_right = terrain[y][x+iw] == 1; 
			w_up = terrain[y-iw][x] == 1; 
			w_down = terrain[y+iw][x] == 1; 
			w_upleft = terrain[y-iw][x-iw] == 1; 
			w_downleft = terrain[y+iw][x-iw] == 1; 
			w_upright = terrain[y-iw][x+iw] == 1; 
			w_downright = terrain[y+iw][x+iw] == 1;
		}

		//Tile values are based on this specific tileset
		tile = 30;
		if(w_up) //top
		{ 
			tile = 27;
			if(w_right) //top, right
			{ 
				tile = 40;
				if(w_down) //top, right, down
				{ 
					tile = 44;
					if(w_left) //top, right, down, left
					{ 
						tile = 15;
						if(w_upright) //top, right, down, left, top-right
						{ 
							tile = 13;
							if(w_downright) //top, right, down, left, top-right, down-right
							{ 
								tile = 8;
								if(w_downleft) //top, right, down, left, top-right, down-right, down-left
								{ 
									tile = 1;
									if(w_upleft) tile = 0; //all 8 sides
								} 
								else if(w_upleft) tile = 4; //top, right, down, left, top-right, down-right, up-left
							} 
							else if(w_downleft) //top, right, down, left, top-right, down-left
							{ 
								tile = 9;
								if(w_upleft) tile = 3; //top, right, down, left, top-right, down-left, top-left
							} 
							else if(w_upleft) tile = 7; //top, right, down, left, top-right, top-left
						} 
						else if(w_downright) //top, right, down, left, down-right
						{ 
							tile = 14;
							if(w_downleft) //top, right, down, left, down-right, down-left
							{ 
								tile = 5;
								if(w_upleft) tile = 2; //top, right, down, left, down-right, down-left, up-left
							} 
							else if(w_upleft) tile = 10; //top, right, down, left, down-right, up-left
						} 
						else if(w_downleft) //top, right, down, left, down-left
						{ 
							tile = 11; 
							if(w_upleft) tile = 6; //top, right, down, left, down-left, up-left
						} 
						else if(w_upleft)tile = 12; //top, right, down, left, up-left
					} 
					else if(w_upright) //top, right, down, up-right
					{ 
						tile = 33;
						if(w_downright)tile = 19; //top, right, down, up-right, down-right
					} 
					else if(w_downright)tile = 36; //top, right, down, down-right
				} 
				else if(w_left) //top, right, left
				{ 
					tile = 43;
					if(w_upright) //top, right, left, up-right
					{ 
						tile = 35;
						if(w_upleft)tile = 18; //top, right, left, up-right, up-left
					} 
					else if(w_upleft)tile = 32; //top, right, left, up-left
				} 
				else if(w_upright)tile = 22; //top, right, up-right
			} 
			else if(w_down) //top, down
			{ 
				tile = 25;
				if(w_left) //top, down, left
				{ 
					tile = 46;
					if(w_downleft) //top, down, left, down-left
					{ 
						tile = 31;
						if(w_upleft)tile = 17; //top, down, left, down-left, up-left
					} 
					else if(w_upleft)tile = 38; //top, down, left, up-left
				} 
			} 
			else if(w_left) //top, left
			{ 
				tile = 39;
				if(w_upleft)tile = 21; //top, left, up-left
			} 
		} 
		else if(w_right) //right
		{ 
			tile = 28;
			if(w_down) //right, down
			{ 
				tile = 41;
				if(w_left) //right, down, left
				{ 
					tile = 45;
					if(w_downright) //right, down, left, down-right
					{ 
						tile = 34;
						if(w_downleft) tile = 16; //right, down, left, down-right, down-left
					} 
					else if(w_downleft)tile = 37; //right, down, left, down-left
				} 
				else if(w_downright)tile = 23; //right, down, down-right
			} 
			else if(w_left) //right, left
			{ 
				tile = 24;
			} 
		} 
		else if(w_down) //down
		{ 
			tile = 29;
			if(w_left) //down, left
			{ 
				tile = 42;
				if(w_downleft)tile = 20; //down, left, down-left
			} 
		} 
		else if(w_left) //left
		{ 
			tile = 26;
		} 

		return tile; 
	}

}
