package com.example.ninemen;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

@SuppressLint("DrawAllocation")
public class BoardView extends View {
	
	private Drawable boardImage;
	private Rect boardBounds = null;
	private Drawable red;
	private Rect redBounds = null;
	private Drawable blue;
	private Rect blueBounds = null;
	
	private ArrayList<Rect> grid;
	
	private Paint bgPaint;

	private static final String redWon = "Red Won";
	private static final String blueWon = "Blue Won";
	private static final String redTurn = "Reds Turn";
	private static final String blueTurn = "Blues Turn";
	private String messagePointer;
	
	private boolean endgame = false;
	
	NineMenMorrisRules rules;
	
	private int markerLifted;
	private boolean markerRemoveable;
	
	public BoardView(Context context) {
		super(context);
		
		// Get a representation of the image
		Resources resources = context.getResources();
		bgPaint = new Paint();
		
		red = (Drawable) resources.getDrawable(R.drawable.red);
		blue = (Drawable) resources.getDrawable(R.drawable.blue);
		boardImage = (Drawable) resources.getDrawable(R.drawable.boardimage);
		
		rules = new NineMenMorrisRules();
		
		grid = new ArrayList<Rect>();

		messagePointer = redTurn;
		
		boardBounds = new Rect(0, 0, 0, 0);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if(endgame){
			return true;
		}
		
		Log.i("TouchView.onTouchEvent", "event = " + event);
		
		if(event.getAction() == MotionEvent.ACTION_DOWN ){
			return true;
		}
		
		if(event.getAction() == MotionEvent.ACTION_UP ) {
			
			
			int x = (int) Math.round(event.getX());
			int y = (int) Math.round(event.getY());

			int boardPos = getBoardPositionForRect(x, y);

			//Log.i("Lol", ""+x+" "+y+" "+ boardPos);

			if(boardPos == 0){
				return true;
			}

			if(rules.getTurn() == NineMenMorrisRules.READ_MOVES){
				//It's reds turn.
				
				if(markerRemoveable){
					//Remove the selected marker
					if(!(markerRemoveable = !rules.remove(boardPos, NineMenMorrisRules.READ_MARKER))){
						messagePointer = redTurn;
					}
				}
				else if(markerLifted > 0){
					//Move the lifted marker to a valid position.
					rules.legalMove(boardPos, markerLifted, NineMenMorrisRules.READ_MOVES);
					markerRemoveable = rules.remove(boardPos);
					markerLifted = 0;
					messagePointer = blueTurn;
				}
				else if(rules.getRedMarker() > 0){
					//Place a marker anywhere on the board (if the position is empty)
					rules.legalMove(boardPos, 0, NineMenMorrisRules.READ_MOVES);
					if(!(markerRemoveable = rules.remove(boardPos))){
						messagePointer = blueTurn;
					}
				}
				else{
					//Select a marker to move
					if(rules.board(boardPos) == NineMenMorrisRules.READ_MARKER){
						markerLifted = boardPos;
						Log.d("LIFTED", "RED BOARDPOS: " + markerLifted);
					}
				}
			}

			else if(rules.getTurn() == NineMenMorrisRules.BLUE_MOVES){
				//It's blues turn.
				
				if(markerRemoveable){
					//Remove the selected marker
					if(!(markerRemoveable = !rules.remove(boardPos, NineMenMorrisRules.BLUE_MARKER))){
						messagePointer = blueTurn;
					}				}
				else if(markerLifted > 0){
					//Move the lifted marker to a valid position.
					rules.legalMove(boardPos, markerLifted, NineMenMorrisRules.BLUE_MOVES);
					markerRemoveable = rules.remove(boardPos);
					markerLifted = 0;
					messagePointer = redTurn;
				}
				else if(rules.getBlueMarker() > 0){
					//Place a marker anywhere on the board (if the position is empty)
					rules.legalMove(boardPos, 0, NineMenMorrisRules.BLUE_MOVES);
					if(!(markerRemoveable = rules.remove(boardPos))){
						messagePointer = redTurn;
					}
				}
				else{
					//Select a marker to move
					if(rules.board(boardPos) == NineMenMorrisRules.BLUE_MARKER){
						markerLifted = boardPos;
						Log.d("LIFTED", "BLUE BOARDPOS: " + markerLifted);
					}
				}
			}
			
			if(rules.win(NineMenMorrisRules.BLUE_MARKER)){
				messagePointer = redWon;
				endgame = true;
			}
			else if(rules.win(NineMenMorrisRules.READ_MARKER)){
				messagePointer = blueWon;
				endgame = true;
			}
			
			Log.i("GAME STATUS", "TURN: " + rules.getTurn());
			Log.i("GAME STATUS", "CLICKED POSITION: " + boardPos);
			Log.i("GAME STATUS", "IS LIFTED: " + markerLifted);
			Log.i("GAME STATUS", "CAN REMOVE: " + markerRemoveable);
			
			// Request the system to redraw the view (call onDraw at 
			// some point in the future)
			// From a non-UI thread, call postInvalidate instead
			invalidate();
			
			return true;
		}
		
		return false;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		
		Log.i("TouchView.onDraw", "");
		
		// Background
		bgPaint.setColor(Color.argb(255, 30, 205, 210));
		canvas.drawPaint(bgPaint);
		
		int w = this.getWidth();
		//Log.d("GETIT", "Width: " + w);
		int h = this.getHeight();
		//Log.d("GETIT", "Heigth: " + h);
		boardBounds.set(0, 0, w, h*44/64);
		
		if(grid.size() == 0){
			for(int j = 0; j < 7; j++){
				for(int i = 0; i < 7; i++){
					grid.add(new Rect(i*w/7, j*(h*11/16)/7, (i+1)*w/7, (j+1)*(h*11/16)/7));
				}
			}
		}
		
		if(boardBounds != null){
			boardImage.setBounds(boardBounds);
			boardImage.draw(canvas);
		}
		
		bgPaint.setColor(Color.argb(233, 22, 222, 22));
		bgPaint.setStyle(Style.STROKE);
		for(Rect r : grid){
			canvas.drawRect(r, bgPaint);
		}
		bgPaint.setStyle(Style.FILL);
		
		//Draw the pieces on the board.
		for(int i = 1; i < 25; i++){
			if(rules.board(i) == NineMenMorrisRules.READ_MARKER){
				redBounds = getRectForMarker(i);
				red.setBounds(redBounds);
				red.draw(canvas);
			}
			else if(rules.board(i) == NineMenMorrisRules.BLUE_MARKER){
				blueBounds = getRectForMarker(i);
				blue.setBounds(blueBounds);
				blue.draw(canvas);
			}
		}
		
		bgPaint.setColor(Color.argb(233, 200, 0, 0));
		bgPaint.setTextSize(60 * this.getResources().getDisplayMetrics().density);
		canvas.drawText(""+rules.getRedMarker(), w/7, h*14/16, bgPaint);
		bgPaint.setColor(Color.argb(233, 0, 0, 200));
		canvas.drawText(""+rules.getBlueMarker(), w*5/7, h*14/16, bgPaint);
		bgPaint.setTextSize(15 * this.getResources().getDisplayMetrics().density);
		bgPaint.setColor(Color.argb(233, 0, 0, 0));
		canvas.drawText(""+messagePointer, w*3/7, h*13/16, bgPaint);
		
	}
	
	private Rect getRectForMarker(int position){
		Rect newRect = new Rect(0, 0, 0, 0);	

		switch (position){
		case 1:
			newRect.set(grid.get(16));
			break;
		case 2:
			newRect.set(grid.get(8));
			break;
		case 3:
			newRect.set(grid.get(0));
			break;
		case 4:
			newRect.set(grid.get(17));
			break;
		case 5:
			newRect.set(grid.get(10));
			break;
		case 6:
			newRect.set(grid.get(3));
			break;
		case 7:
			newRect.set(grid.get(18));
			break;
		case 8:
			newRect.set(grid.get(12));
			break;
		case 9:
			newRect.set(grid.get(6));
			break;
		case 10:
			newRect.set(grid.get(25));
			break;
		case 11:
			newRect.set(grid.get(26));
			break;
		case 12:
			newRect.set(grid.get(27));
			break;
		case 13:
			newRect.set(grid.get(32));
			break;
		case 14:
			newRect.set(grid.get(40));
			break;
		case 15:
			newRect.set(grid.get(48));
			break;
		case 16:
			newRect.set(grid.get(31));
			break;
		case 17:
			newRect.set(grid.get(38));
			break;
		case 18:
			newRect.set(grid.get(45));
			break;
		case 19:
			newRect.set(grid.get(30));
			break;
		case 20:
			newRect.set(grid.get(36));
			break;
		case 21:
			newRect.set(grid.get(42));
			break;
		case 22:
			newRect.set(grid.get(23));
			break;
		case 23:
			newRect.set(grid.get(22));
			break;
		case 24:
			newRect.set(grid.get(21));
			break;
		}
		
		return newRect;
	}
	
	private int getBoardPositionForRect(int x, int y){
		int rectPos = -1;
		for(Rect r : grid){
			rectPos++;
			if(x > r.left && x < r.right && y > r.top && y < r.bottom){
				Log.i("TAG", ""+ x + " > " + r.left + " && " + x + " < " + r.right + " && " + y + " > " + r.top + " && " + y + " < " + r.bottom);
				switch (rectPos){
				case 16:
					return 1;
				case 8:
					return 2;
				case 0:
					return 3;
				case 17:
					return 4;
				case 10:
					return 5;
				case 3:
					return 6;
				case 18:
					return 7;
				case 12:
					return 8;
				case 6:
					return 9;
				case 25:
					return 10;
				case 26:
					return 11;
				case 27:
					return 12;
				case 32:
					return 13;
				case 40:
					return 14;
				case 48:
					return 15;
				case 31:
					return 16;
				case 38:
					return 17;
				case 45:
					return 18;
				case 30:
					return 19;
				case 36:
					return 20;
				case 42:
					return 21;
				case 23:
					return 22;
				case 22:
					return 23;
				case 21:
					return 24;
				}
			}
		}
		

		
		return 0;
	}
}
