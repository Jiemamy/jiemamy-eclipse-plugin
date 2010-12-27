/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
 * Created on 2008/07/15
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

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * 設定の読み書きインターフェイス。
 * @author daisuke
 */
public interface JiemamyPreference {
	
	/**
	 * コネクションルータの種類を取得する。
	 * 
	 * @return コネクションルータの種類
	 */
	ConnectionRouters getConnectionRouter();
	
	/**
	 * プリファレンスストアを取得する。
	 * 
	 * @return プリファレンスストア
	 */
	IPreferenceStore getPreferenceStore();
	
	/**
	 * 外部キー接続時に、参照側テーブルに被参照テーブルの主キーと同名のカラムを作成するかどうかを取得する。
	 * 
	 * @return 外部キー接続時に、参照側テーブルに被参照テーブルの主キーと同名のカラムを作成するかどうか
	 */
	boolean isCreateColumnWithFk();
	
	/**
	 * Preference Storeをデフォルト値で初期化する。
	 */
	void loadDefaultValues();
	
	/**
	 * コネクションルータの種類を設定する。
	 * 
	 * @param connectionRouters コネクションルータの種類 
	 */
	void setConnectionRouter(ConnectionRouters connectionRouters);
	
	/**
	 * 外部キー接続時に、参照側テーブルに被参照テーブルの主キーと同名のカラムを作成するかどうかを設定する。
	 * 
	 * @param createColumnWithFk 外部キー接続時に、参照側テーブルに被参照テーブルの主キーと同名のカラムを作成するかどうか
	 */
	void setCreateColumnWithFk(boolean createColumnWithFk);
}
