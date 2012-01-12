/*
 * Copyright 2007-2012 Jiemamy Project and the Others.
 * Created on 2008/07/28
 *
 * This file is part of Jiemamy.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.jiemamy.eclipse.core.ui.preference;

import org.eclipse.draw2d.BendpointConnectionRouter;
import org.eclipse.draw2d.ConnectionRouter;
import org.eclipse.draw2d.FanRouter;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ManhattanConnectionRouter;
import org.eclipse.draw2d.ShortestPathConnectionRouter;

/**
 * コネクションルータの種類。
 * 
 * @author daisuke
 */
public enum ConnectionRouters {
	
	/** BendPointルータ */
	BENDPOINT(Messages.Bendpoint_Connection_Router) {
		
		@Override
		public ConnectionRouter getRouter(IFigure figure) {
			FanRouter fanRouter = getFanRouter();
			fanRouter.setNextRouter(new BendpointConnectionRouter());
			return fanRouter;
		}
	},
	
	/** ShortestPathルータ */
	SHORTEST(Messages.Shortest_Path_Connection_Router) {
		
		@Override
		public ConnectionRouter getRouter(IFigure figure) {
			FanRouter fanRouter = getFanRouter();
			fanRouter.setNextRouter(new ShortestPathConnectionRouter(figure));
			return fanRouter;
		}
		
	},
	
	/** Manhattanルータ */
	MANHATTAN(Messages.Manhattan_Connection_Router) {
		
		@Override
		public ConnectionRouter getRouter(IFigure figure) {
			return new ManhattanConnectionRouter();
		}
	};
	
	/** ファンルータの開き具合 */
	private static final int FAN_ROUTER_SEPARATION = 50;
	
	private static final String[] LABELS = new String[values().length];
	
	static {
		int count = 0;
		for (ConnectionRouters router : values()) {
			LABELS[count++] = router.label;
		}
	}
	
	
	/**
	 * ラベルからenumインスタンスを取得する。
	 * 
	 * @param label ラベル
	 * @return enumインスタンス
	 * @throws IllegalArgumentException 該当するenumインスタンスが見つからない場合
	 */
	public static ConnectionRouters get(String label) {
		for (ConnectionRouters router : values()) {
			if (router.label.equals(label)) {
				return router;
			}
		}
		throw new IllegalArgumentException(label);
	}
	
	/**
	 * labelsを取得する。
	 * 
	 * @return LABELS
	 */
	public static String[] getLabels() {
		return LABELS.clone();
	}
	
	private static FanRouter getFanRouter() {
		FanRouter fanRouter = new FanRouter();
		fanRouter.setSeparation(FAN_ROUTER_SEPARATION);
		return fanRouter;
	}
	
	
	/** ラベル文字列 */
	private String label;
	
	
	ConnectionRouters(String label) {
		this.label = label;
	}
	
	/**
	 * ラベル文字列を取得する。
	 * 
	 * @return ラベル文字列
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * コネクションルータを取得する。
	 * 
	 * @param figure フィギュア
	 * @return コネクションルータ
	 */
	public abstract ConnectionRouter getRouter(IFigure figure);
}
