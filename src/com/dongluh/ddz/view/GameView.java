package com.dongluh.ddz.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.IntentSender.SendIntentException;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.GestureDetector.OnDoubleTapListener;

import com.dongluh.ddz.R;
import com.dongluh.ddz.model.Card;
import com.dongluh.ddz.model.Player;

/**
 * @author Administrator
 *
 */
/**
 * @author Administrator
 *
 */
public class GameView extends BaseView {

	public static Bitmap backCard; 	// 背景
	private Bitmap background;  // 牌的背面
	
	public static final int PLAYER_MARGIN = 10;    // 玩家和屏幕边框的距离
	
	private List<Player> players   = new ArrayList<Player>(); 	// 3个玩家
	private List<Card> cards       = new ArrayList<Card>();     // 一副牌
	private List<Card> bottomCards = new ArrayList<Card>(); 	// 3张底牌
	// private Rect bgRect;
	
	public static int cardWidth; 		// 一张牌的宽度
	public static int cardHeight; 		// 一张牌的宽度
	public static int pokerWidth; 		// 整副牌的宽度
	public static int pokerHeight; 		// 整副牌的宽度
	
	public static int miniCardWidth; 	// 一张小牌的宽度
	public static int miniCardHeight; 	// 一张小牌的宽度
	public static int miniPokerWidth; 	// 整副小牌的宽度
	public static int miniPokerHeight; 	// 整副小牌的宽度
	
	public static int cardMargin;    // 牌之间的间距
	
	private Player me;
	
	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initRes((Activity) context);
	
		initPlayer();
		
		intiCards();
		
		startNewGame();
	}
	
	
	GestureDetector detector;
	private void initRes(Activity activity) {
		background = getBitmap(R.drawable.bg, winWidth, winHeight);
		// bgRect = new Rect(0, 0, width, height);
		OnGestureListener gestureListener = new OnGestureListener() {
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void onShowPress(MotionEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
					float distanceY) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void onLongPress(MotionEvent e) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
					float velocityY) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean onDown(MotionEvent e) {
				// TODO Auto-generated method stub
				return false;
			}
		};
		detector = new GestureDetector(gestureListener);
		detector.setOnDoubleTapListener(this);
	}

	private void startNewGame() {
		cleanCards();
		distributeCards();
	}
	
	private void initPlayer() {
		// 自己
		me = new Player();
		me.setComputer(false);
		me.setName("donglu");
		me.setLandlord(true);
		me.setScore("0");
		Bitmap myHead = BitmapFactory.decodeResource(getResources(), R.drawable.me);
		me.setHead(myHead);
		me.setX(PLAYER_MARGIN);
		me.setY(winHeight- myHead.getHeight() - TEXT_SIZE - 100);

		// 左边
		Player left = new Player();
		left.setComputer(true);
		left.setName("马化騰");
		left.setLandlord(false);
		left.setScore("0");
		Bitmap leftHead = BitmapFactory.decodeResource(getResources(), R.drawable.left);
		left.setHead(leftHead);
		left.setX(PLAYER_MARGIN);
		left.setY(PLAYER_MARGIN);
		
		// 右边
		Player right = new Player();
		right.setComputer(true);
		right.setName("乔布斯");
		right.setLandlord(false);
		right.setScore("543210");
		Bitmap rightHead = BitmapFactory.decodeResource(getResources(), R.drawable.right);
		right.setHead(rightHead);
		right.setX(winWidth - rightHead.getWidth() - PLAYER_MARGIN);
		right.setY(PLAYER_MARGIN);
		
		/**
		 * 设置下家
		 */
		me.setNext(right);
		right.setNext(left);
		left.setNext(me);
		
		players.add(me);
		players.add(left);
		players.add(right);
	}


	private void intiCards() {
		// 初始化牌的尺寸
		cardWidth = winWidth / 13;
		cardHeight = winHeight / 6;
		pokerWidth = cardWidth * 13;
		pokerHeight = cardHeight * 5;

		// 初始化小牌尺寸
		miniCardHeight = cardHeight * 3 / 5;
		miniCardWidth = cardWidth * 3 / 5;
		miniPokerWidth = miniCardWidth * 13;
		miniPokerHeight = miniCardHeight * 5;
		
		cardMargin = cardWidth * 3 / 5;
		
		// 整副牌
		Bitmap poker = getBitmap(R.drawable.poker, pokerWidth, pokerHeight);
		// 整副小牌
		Bitmap pokerMini = getBitmap(R.drawable.poker_mini, miniPokerWidth,
				miniPokerHeight);
		// 卡片背面
		backCard = Bitmap.createBitmap(pokerMini, 2 * miniCardWidth,
				4 * miniCardHeight, miniCardWidth, miniCardHeight);
		
		// 把牌切割出来 
		for (int row = 0; row < 5; row++) {
			for (int col = 0; col < 13; col++) {
				if (row == 4 && col > 1) {
					break;
				}
				
				Bitmap bigCard = Bitmap.createBitmap(poker, col * cardWidth,
						row * cardHeight, cardWidth, cardHeight);

				Bitmap miniCard = Bitmap.createBitmap(pokerMini, col
						* miniCardWidth, row * miniCardHeight, miniCardWidth,
						miniCardHeight);
				
				Card card = new Card();
				card.setBigImg(bigCard);
				card.setSmallImg(miniCard);
				card.setColor(row);
				card.setValue(col + 1);

				if (col < 2) { 
					card.setValue(card.getValue() + 13); // 14表示A，15表示2
					if (row == 4) { // 大小王
						card.setValue(col == 0 ? 17 : 16); // 16表示小王，17表示大王
					}
				}
				cards.add(card);
			}
		}
	}

	/**
	 * 洗牌
	 */
	public void cleanCards() { // 随机调换卡片位置
		for (int i = 0; i < 100; i++) {
			int rdIndex = (int) (Math.random() * cards.size());
			if (rdIndex != 0) {
				// 分别取出两张卡片
				Card aCard = cards.get(rdIndex);
				Card bCard = cards.get(0);

				cards.set(0, aCard); // 交换卡片位置
				cards.set(rdIndex, bCard);
			}
		}
	}
	
	/**
	 * 发牌
	 */
	private void distributeCards() {
		// 先清理所有玩家的牌
		for (Player player : players) {
			player.getSentCards().clear();
			player.getHandCards().clear();
			player.getWillSendCards().clear();
		}

		// 给每个玩家17张
		for (int i = 0; i < 17; i++) {
			for (int j = 0; j < players.size(); j++) {
				players.get(j).getHandCards().add(cards.get(3 * i + j));
			}
		}

		// 剩下3张作为底牌
		bottomCards.clear();
		bottomCards.add(cards.get(51));
		bottomCards.add(cards.get(52));
		bottomCards.add(cards.get(53));
		sortCards(bottomCards); // 对底牌排序

		// 手牌排序
		for (Player player : players) {
			if (player.isLandlord()) {
				// 给底牌给地主
				player.getHandCards().addAll(bottomCards);
			}
			sortCards(player.getHandCards()); 
		}
	}

	private void sortCards(List<Card> cards) {
		Collections.sort(cards, this);
	}

	@Override
	protected void render(Canvas canvas) {
		// 画背景
		canvas.drawBitmap(background, 0, 0, paint);     
 
		// 画玩家
		synchronized (players) {
			for (Player p : players) {
				p.draw(canvas, paint);
			}
		}
		
		// 底牌的宽度
		int bottomCardWidth = miniCardWidth * bottomCards.size();
		// 底牌开始的X坐标
		int bottomCardStartX = (winWidth - bottomCardWidth) / 2;
		
		for (int i = 0; i < bottomCards.size(); i++) {
			//取出小牌
			Bitmap miniIcon = bottomCards.get(i).getSmallImg();
			//画小牌
			canvas.drawBitmap(miniIcon, bottomCardStartX + i*miniCardWidth, 0, paint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float y = event.getY();
		float x = event.getX();
		
		if(event.getAction() == MotionEvent.ACTION_DOWN) {
			//计算手牌宽度
			int handCardsWidth  = (me.getHandCards().size()-1) * GameView.cardMargin + GameView.cardWidth;
			//计算手牌开始的X
			int handCardStartX = (GameView.winWidth - handCardsWidth) / 2;
			//看是否在手牌范围内
			if ((y >= winHeight - cardHeight - GameView.PLAYER_MARGIN) 
					&& (x >= handCardStartX)
					&& (x <= winWidth - handCardStartX)) {

				int index = ((int)x - handCardStartX)/cardMargin; 
				if(index > me.getHandCards().size()-1) { //判断是否越界
					index = me.getHandCards().size()-1;
				}
				
				Card card = me.getHandCards().get(index);
				
				if(me.getWillSendCards().contains(card)) { //说明已经被提起来，现在要放下去
					me.getWillSendCards().remove(card);
				} else {//说明要提起来
					me.getWillSendCards().add(card);
				}
			}
		}
		return detector.onTouchEvent(event);
	}
	
	public Bitmap getBitmap(int resId, int w, int h) {
		Bitmap bitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Drawable drawable = getResources().getDrawable(resId);
		drawable.setBounds(0, 0, w, h);
		drawable.draw(canvas);
		return bitmap;
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		System.out.println("onDoubleTap");
		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		System.out.println("onDoubleTapEvent");
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		System.out.println("onSingleTapConfirmed");
		return false;
	}
}
