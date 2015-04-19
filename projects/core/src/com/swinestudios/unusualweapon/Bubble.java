package com.swinestudios.unusualweapon;

import org.mini2Dx.core.geom.Rectangle;
import org.mini2Dx.core.graphics.Graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Bubble {
	
	public final float NET_VELOCITY = 1.5f;
	
	public float framesAtZero = 0;
	
	
	public final float LIFESPAN = 10f;
	public float timeAlive = 0f;
	
	
	public float x, y;
	public float velX, velY;
	public float accelX;
	public float accelY = -1f;

	public final float frictionX = 0.4f;
	public final float frictionY = 0.4f;

	public final float moveSpeedX = 2.0f;
	public final float moveSpeedY = 2.0f;

	public final float maxSpeedX = 2.0f;
	public final float maxSpeedY = 2.0f;

	public boolean isActive;
	public boolean delete = false;


	public Rectangle hitbox;
	public Gameplay level;
	public String type;
	
	public Bubble(float startX, float startY, float targetX, float targetY, Gameplay level){
		//System.out.println(startX + " " + startY + " " + targetX + " " + targetY);
		hitbox = new Rectangle(x, y, 16, 16); 
		this.x = startX-this.hitbox.width/2;
		this.y = startY-this.hitbox.height/2;
		float c = (float) Math.pow(Math.pow(targetX-startX, 2) + Math.pow(targetY-startY, 2), 0.5);
		float xComponent = (targetX-startX)/c;
		float yComponent = (targetY-startY)/c;
		
		float offset = (float) (Math.toRadians(30.0 * (Math.random() * 2 - 1.0)));
		float cs = (float) Math.cos(offset);
		float sn = (float) Math.sin(offset);
		
		float px = xComponent * cs - yComponent * sn;
		float py = xComponent * sn + yComponent * cs;
		
		this.velX = NET_VELOCITY * px;
		this.velY = NET_VELOCITY * py;
		accelX = 0;
		accelY = -0.009f;
		isActive = false;
		this.level = level;
		type = "Bubble";
	}
	
	public void render(Graphics g){
		g.setColor(Color.RED);
		g.drawRect(x, y, this.hitbox.width, this.hitbox.height);
		g.setColor(Color.WHITE);
	}
	
	
	public void update(float delta){
		accelX = 0;

		timeAlive += delta;
		if(timeAlive > LIFESPAN){
			delete = true;
		}


		//friction(true, true);

		limitSpeed(true, false);
		move();
		hitbox.setX(this.x);
		hitbox.setY(this.y);
		
		
		if(this.velY == -0.009f){
			framesAtZero++;
		}
		
		if(framesAtZero >= 2){
			delete = true;
		}
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

	/*
	 * Helper method for checking whether there is a collision if the player moves at the given position
	 */
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

	/*
	 * Sets up any images that the player may have. Necessary because images are flipped and have the origin
	 * on the bottom-left by default.
	 */
	public void adjustSprite(Sprite... s){
		for(int i = 0; i < s.length; i++){
			s[i].setOrigin(0, 0);
			s[i].flip(false, true);
		}
	}
}
