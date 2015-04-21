package com.swinestudios.unusualweapon;

import org.mini2Dx.core.geom.Rectangle;
import org.mini2Dx.core.graphics.Animation;
import org.mini2Dx.core.graphics.Graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Enemy {

	public float x, y;
	public float velX, velY;
	public float accelX, accelY;

	public final float frictionX = 0.5f;
	public final float frictionY = 0.7f;

	public final float moveSpeedX = 1.0f;
	public final float moveSpeedY = 1.0f;

	public final float maxSpeedX = 1.2f;
	public final float maxSpeedY = 1.2f;

	public boolean isActive;

	public boolean facingRight, facingLeft;

	public final float totalReloadTime = 10;
	public float elapsedReloadTime = 0;


	//PATHING STUFF

	public float timeSinceChange = 0;
	public final float TIMETOCHANGE = 8f;

	public final float LEASH_DISTANCE = 200f;
	public final float CHASING_ACCEL = 2f;
	public final float BURST_SPEED = 3f;

	public boolean chasing = false;//Whether or not it is actively trying to seek out the player.

	public boolean bubbled = false;
	public float bubbleTimer = 0f;
	public float maxBubbleTimer = 4f; //How many seconds an enemy stays bubbled

	//-------------------------------


	//HEALTH AND HEALTHBAR STUFF
	/*public float maxHealth = 100.0f;
	public float health = maxHealth;

	public float healthBarMaxWidth = 200;
	public float healthBarHeight = 16;

	//Obsolete - public float healthBarX = 400;
	public float healthBarY = 16;

	public float healthBarRed = 0;
	public float healthBarGreen = 0;
	public float healthBarBlue = 0;*/
	//-----------------------------------

	public boolean delete = false;

	public Rectangle hitbox;
	public Gameplay level;
	public String type;

	public Sprite left1, left2, right1, right2;
	public Animation<Sprite> fishLeft, fishRight, fishCurrent;
	public float animationSpeed = 0.1f; //How many seconds a frame lasts

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
		facingRight = true;
		facingLeft = false;
		
		right1 = new Sprite(new Texture(Gdx.files.internal("fishFrames/fish_right_1.png")));
		right2 = new Sprite(new Texture(Gdx.files.internal("fishFrames/fish_right_2.png")));
		
		left1 = new Sprite(new Texture(Gdx.files.internal("fishFrames/fish_left_1.png")));
		left2 = new Sprite(new Texture(Gdx.files.internal("fishFrames/fish_left_2.png")));
		
		Global_Constants.adjustSprite(right1, right2, left1, left2);
		
		fishLeft = new Animation<Sprite>(); //left animation
		fishRight = new Animation<Sprite>(); //right animation
		
		fishLeft.addFrame(left1, animationSpeed);
		fishLeft.addFrame(left2, animationSpeed);
		fishLeft.setLooping(true);
		fishLeft.flip(false, true);
		
		fishRight.addFrame(right1, animationSpeed);
		fishRight.addFrame(right2, animationSpeed);
		fishRight.setLooping(true);
		fishRight.flip(false, true);
		
		Global_Constants.setFrameSizes(fishLeft, left1.getWidth() * 2, left1.getHeight() * 2);
		Global_Constants.setFrameSizes(fishRight, right1.getWidth() * 2, right1.getHeight() * 2);
		
		fishCurrent = fishRight;
		
		hitbox = new Rectangle(x, y, right1.getWidth(), right1.getHeight());
	}

	public void update(float delta){

		//accelX = 0;
		//accelY = 0;

		pathing(delta);

		//Apply friction when not moving or when exceeding the max horizontal speed
		friction(true, true);

		limitSpeed(true, true);
		bubbleContact();
		bubbleEffect();
		move();
		hitbox.setX(this.x);
		hitbox.setY(this.y);
		
		updateSprite(delta);

		if(bubbled){
			bubbleTimer += delta;
			if(bubbleTimer > maxBubbleTimer){
				bubbleTimer = 0;
				bubbled = false;
			}
		}
	}

	public void render(Graphics g){
		fishCurrent.draw(g, x, y);
	}
	
	public void updateSprite(float delta){
		if(velX >= 0){
			facingRight = true;
			facingLeft = false;
		}
		else{
			facingRight = false;
			facingLeft = true;
		}
		//change the direction the fish is facing
		if(facingRight){
			fishCurrent = fishRight;
		}
		else{
			fishCurrent = fishLeft;
		}
		fishCurrent.update(delta);
	}
	
	public void bubbleEffect(){
		if(bubbled){
			accelY = -0.705f;
			accelX = 0f;
		}
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
				System.out.println("LEFT");
				swimLeft();
				break;

			case 1:
				System.out.println("RIGHT");
				swimRight();
				break;

			default:

				//Do nothing, this shouldn't happen.
				break;
			}
		}

	}

	public void swimLeft(){
		accelX = -2f;
		velX = -BURST_SPEED;
	}

	public void swimRight(){
		accelX = 2f;
		velX = BURST_SPEED;
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
