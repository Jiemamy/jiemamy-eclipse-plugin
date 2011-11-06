/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2008/07/29
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
package org.jiemamy.eclipse.core.ui.editor;

import org.jiemamy.model.JmDiagram;
import org.jiemamy.model.Level;
import org.jiemamy.model.Mode;

/**
 * ER図の表示モードを表す列挙型。
 * 
 * @author daisuke
 */
enum DisplayStatus {
	
	/** 物理モデル：属性/型レベル */
	PHYSICAL_ATTRTYPE(Mode.PHYSICAL, Level.ATTRTYPE, Messages.Physical_AttrAndType),
	
	/** 物理モデル：属性レベル */
	PHYSICAL_ATTR(Mode.PHYSICAL, Level.ATTR, Messages.Physical_Attribute),
	
	/** 物理モデル：識別子レベル */
	PHYSICAL_KEY(Mode.PHYSICAL, Level.KEY, Messages.Physical_Key),
	
	/** 物理モデル：エンティティレベル */
	PHYSICAL_ENTITY(Mode.PHYSICAL, Level.ENTITY, Messages.Physical_Entity),
	
	/** 論理モデル：属性/型レベル */
	LOGICAL_ATTRTYPE(Mode.LOGICAL, Level.ATTRTYPE, Messages.Logical_AttrAndType),
	
	/** 論理モデル：属性レベル */
	LOGICAL_ATTR(Mode.LOGICAL, Level.ATTR, Messages.Logical_Attribute),
	
	/** 論理モデル：識別子レベル */
	LOGICAL_KEY(Mode.LOGICAL, Level.KEY, Messages.Logical_Key),
	
	/** 論理モデル：エンティティレベル */
	LOGICAL_ENTITY(Mode.LOGICAL, Level.ENTITY, Messages.Logical_Entity);
	
	/**
	 * ダイアグラム表現に設定されたモードとレベルから、適切なステータスを探す。
	 * 
	 * @param diagram ダイアグラム
	 * @return ステータス. 見つからなかった場合は{@code null}
	 */
	public static DisplayStatus find(JmDiagram diagram) {
		for (DisplayStatus status : values()) {
			if (diagram.getMode() == status.mode && diagram.getLevel() == status.level) {
				return status;
			}
		}
		return null;
	}
	
	
	/** 論理モデルかどうか */
	private Mode mode;
	
	/** 表示レベル */
	private Level level;
	
	/** ラベル文字列 */
	private String label;
	
	
	DisplayStatus(Mode mode, Level level, String label) {
		this.mode = mode;
		this.level = level;
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
	 * 表示レベルを取得する。
	 * 
	 * @return 表示レベル
	 */
	public Level getLevel() {
		return level;
	}
	
	/**
	 * 論理モデルかどうかを取得する。
	 * 
	 * @return 論理モデルかどうか
	 */
	public Mode getMode() {
		return mode;
	}
}
