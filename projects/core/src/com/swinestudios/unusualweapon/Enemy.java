package com.swinestudios.unusualweapon;

import org.mini2Dx.core.geom.Rectangle;
import org.mini2Dx.core.graphics.Graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;

public class Enemy {

	public float x, y;
	public float velX, velY;
	public float accelX, accelY;

	public final float frictionX = 0.7f;
	public final float frictionY = 0.7f;

	public final float moveSpeedX = 2.0f;
	public final float moveSpeedY = 2.0f;

	public final float maxSpeedX = 2.0f;
	public final float maxSpeedY = 2.0f;

	public boolean isActive;

	public boolean facingRight, facingLeft;

	public final int totalBubbleAmmo = 8;
	public int bubbleAmmo = 8;
	public final float totalReloadTime = 10;
	public float elapsedReloadTime = 0;


	//PATHING STUFF

	public float timeSinceChange = 0;
	public final float TIMETOCHANGE = 8f;

	public final float LEASH_DISTANCE = 100f;
	public final float CHASING_ACCEL = 1f;

	public boolean chasing = false;//Whether or not it is actively trying to seek out the player.

	public boolean bubbled = false;

	//-------------------------------


	//HEALTH AND HEALTHBAR STUFF
	public float maxHealth = 100.0f;
	public float health = maxHealth;

	public float healthBarMaxWidth = 200;
	public float healthBarHeight = 16;

	//Obsolete - public float healthBarX = 400;
	public float healthBarY = 16;

	public float healthBarRed = 0;
	public float healthBarGreen = 0;
	public float healthBarBlue = 0;
	//-----------------------------------

	public boolean delete = false;

	public Rectangle hitbox;
	public Gameplay level;
	public String type;



	public Enemy(float x, float y, float width, float height, Gameplay level){
		this.x = x;
		this.y = y;
		hitbox = new Rectangle(x, y, width, height); 
		velX = 0;
		velY = 0;
		accelX = 0;
		accelY = 0;
		isActive = false;
		this.level = level;
		type = "Enemy";
	}



	public void update(float delta){



		//System.out.println(x + " "  + y);

		pathing(delta);

		//Apply friction when not moving or when exceeding the max horizontal speed
		friction(true, true);

		limitSpeed(true, true);
		move();
		hitbox.setX(this.x);
		hitbox.setY(this.y);

	}



	public void render(Graphics g){



		g.setColor(Color.RED);
		g.fillRect(x, y, this.hitbox.width, this.hitbox.height);
		g.setColor(Color.WHITE);
	}

	public void bubbleContact(){

		if(bubbled == false){//bubbled enemies shouldn't remove other bubbles. TODO consider changing this.
			for(int i = 0; i<level.bubbles.size(); i++){
				if(colliding(level.bubbles.get(i).hitbox)){
					bubbled = true;
					//Another function will apply affects based on whether or not the enemy is bubbled


					level.bubbles.remove(i);
				}
			}
		}

	}

	public boolean colliding(Rectangle input){
		if(this.hitbox.intersects(input)){
			return true;
		}

		else{
			return false;
		}
	}

	public void pathing( float delta ){
		if(distanceTo(level.player.hitbox) <= LEASH_DISTANCE){
			chasing = true;
		}
		else{
			chasing = false;
		}

		if(chasing == true){
			accelX = CHASING_ACCEL * Global_Constants.unitVectorX(level.player.x - this.x, level.player.y - this.y);
			accelY = CHASING_ACCEL * Global_Constants.unitVectorY(level.player.x - this.x, level.player.y - this.y);
		}

		timeSinceChange += delta;

		if(timeSinceChange >= TIMETOCHANGE && chasing == false){
			//System.out.println("Execute");

			timeSinceChange = 0;
			switch(Global_Constants.random.nextInt(2)){
			case 0:
				swimLeft();
				break;

			case 1:
				swimRight();
				break;

			default:

				//Do nothing, this shouldn't happen.

			}
		}

	}

	public void swimLeft(){
		accelX = -1f;
	}

	public void swimRight(){
		accelX = 1f;
	}

	public boolean isColliding(Rectangle other, float x, float y){
		if(other == this.hitbox){ //Make sure solid isn't stuck on itself
			return false;
		}
		if(x < other.x + other.width && x + hitbox.width > other.x && y < other.y + other.height && y + hitbox.height > other.y){
			return true;
		}
		return false;
	}

	public boolean collisionExistsAt(float x, float y){
		for(int i = 0; i < level.solids.size(); i++){
			Rectangle solid = level.solids.get(i);
			if(isColliding(solid, x, y)){
				return true;
			}
		}
		return false;
	}

	public void move(){
		moveX();
		moveY();
	}

	/*
	 * Applies a friction force in the given axes by subtracting the respective velocity components
	 * with the given friction components.
	 */
	public void friction(boolean horizontal, boolean vertical){
		//if there is horizontal friction
		if(horizontal){
			if(velX > 0){
				velX -= frictionX; //slow down
				if(velX < 0){
					velX = 0;
				}
			}
			if(velX < 0){
				velX += frictionX; //slow down
				if(velX > 0){
					velX = 0;
				}
			}
		}
		//if there is vertical friction
		if(vertical){
			if(velY > 0){
				velY -= frictionY; //slow down
				if(velY < 0){
					velY = 0;
				}
			}
			if(velY < 0){
				velY += frictionY; //slow down
				if(velY > 0){
					velY = 0;
				}
			}
		}
	}

	/*
	 * Limits the speed of the player to a set maximum
	 */
	protected void limitSpeed(boolean horizontal, boolean vertical){
		//If horizontal speed should be limited
		if(horizontal){
			if(Math.abs(velX) > maxSpeedX){
				velX = maxSpeedX * Math.signum(velX);
			}
		}
		//If vertical speed should be limited
		if(vertical){
			if(Math.abs(velY) > maxSpeedY){
				velY = maxSpeedY * Math.signum(velY);
			}
		}
	}

	/*
	 * Returns the current tile position of the player, given the specific tile dimensions
	 */
	public float getTileX(int tileSize){
		return (int)(x / tileSize) * tileSize;
	}

	/*
	 * Returns the current tile position of the player, given the specific tile dimensions
	 */
	public float getTileY(int tileSize){
		return (int)(y / tileSize) * tileSize;
	}

	/*
	 * Returns the distance between the player and the given target
	 */
	public float distanceTo(Rectangle target){
		return ((float)Math.pow(Math.pow((target.y - this.y), 2.0) + Math.pow((target.x - this.x), 2.0), 0.5));
	}

	/*
	 * Move horizontally in the direction of the x-velocity vector. If there is a collision in
	 * this direction, step pixel by pixel up until the player hits the solid.
	 */
	public void moveX(){
		for(int i = 0; i < level.solids.size(); i++){
			Rectangle solid = level.solids.get(i);
			if(isColliding(solid, x + velX, y)){
				while(!isColliding(solid, x + Math.signum(velX), y)){
					x += Math.signum(velX);
				}
				velX = 0;
			}
		}
		x += velX;
		velX += accelX;
	}

	/*
	 * Move vertically in the direction of the y-velocity vector. If there is a collision in
	 * this direction, step pixel by pixel up until the player hits the solid.
	 */
	public void moveY(){
		for(int i = 0; i < level.solids.size(); i++){
			Rectangle solid = level.solids.get(i);
			if(isColliding(solid, x, y + velY)){
				while(!isColliding(solid, x, y + Math.signum(velY))){
					y += Math.signum(velY);
				}
				velY = 0;
			}
		}
		y += velY;
		velY += accelY;
	}



}
