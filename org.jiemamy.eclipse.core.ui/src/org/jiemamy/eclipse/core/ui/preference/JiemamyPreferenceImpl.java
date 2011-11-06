/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
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

import org.apache.commons.lang.text.StrBuilder;
import org.eclipse.jface.preference.IPreferenceStore;

import org.jiemamy.eclipse.core.ui.JiemamyUIPlugin;

/**
 * 設定の読み書き実装クラス。
 * 
 * @author daisuke
 */
public class JiemamyPreferenceImpl implements JiemamyPreference {
	
	private IPreferenceStore ps = JiemamyUIPlugin.getDefault().getPreferenceStore();
	
	
	public ConnectionRouters getConnectionRouter() {
		return ConnectionRouters.valueOf(ps.getString(PreferenceKey.CONNECTION_ROUTER.toString()));
	}
	
	public IPreferenceStore getPreferenceStore() {
		return ps;
	}
	
	public boolean isCreateColumnWithFk() {
		return ps.getBoolean(PreferenceKey.CREATE_COLUMNS_WITH_FK.toString());
	}
	
	public void loadSimpleValues() {
		ps.setValue(PreferenceKey.CREATE_COLUMNS_WITH_FK.toString(),
				PreferenceKey.CREATE_COLUMNS_WITH_FK.getDefaultBoolean());
		ps.setValue(PreferenceKey.CONNECTION_ROUTER.toString(), PreferenceKey.CONNECTION_ROUTER.getDefaultString());
	}
	
	public void setConnectionRouter(ConnectionRouters connectionRouters) {
		ps.setValue(PreferenceKey.CONNECTION_ROUTER.toString(), connectionRouters.name());
	}
	
	public void setCreateColumnWithFk(boolean createColumnOnFk) {
		ps.setValue(PreferenceKey.CREATE_COLUMNS_WITH_FK.toString(), createColumnOnFk);
	}
	
	@Override
	public String toString() {
		StrBuilder sb = new StrBuilder();
		
		sb.append(PreferenceKey.CONNECTION_ROUTER).append("=").appendln(getConnectionRouter());
		sb.append(PreferenceKey.CREATE_COLUMNS_WITH_FK).append("=").appendln(isCreateColumnWithFk());
		
		return sb.toString();
	}
}
