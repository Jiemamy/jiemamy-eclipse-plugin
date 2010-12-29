/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
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

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

import org.jiemamy.eclipse.core.ui.JiemamyUIPlugin;

/**
 * Jiemamy Eclipse Plugin のプリファレンス値に対するデフォルト情報を与える。
 * 
 * @author daisuke
 */
public class JiemamyPreferenceInitializer extends AbstractPreferenceInitializer {
	
	@Override
	public void initializeDefaultPreferences() {
//		String workspacePath = ResourcesPlugin.getWorkspace().getRoot().getRawLocation().toOSString();
		Preferences preferences = JiemamyUIPlugin.getDefault().getPluginPreferences();
		
		preferences.setDefault(PreferenceKey.CREATE_COLUMNS_WITH_FK.toString(), PreferenceKey.CREATE_COLUMNS_WITH_FK
			.getDefaultBoolean());
		preferences.setDefault(PreferenceKey.CONNECTION_ROUTER.toString(), PreferenceKey.CONNECTION_ROUTER
			.getDefaultString());
	}
}
