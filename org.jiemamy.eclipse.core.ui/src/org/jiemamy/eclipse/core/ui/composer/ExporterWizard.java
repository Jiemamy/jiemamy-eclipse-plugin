/*
 * Copyright 2007-2012 Jiemamy Project and the Others.
 * Created on 2009/02/25
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
package org.jiemamy.eclipse.core.ui.composer;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.IFileEditorInput;

import org.jiemamy.composer.ExportConfig;
import org.jiemamy.composer.Exporter;

/**
 * エクスポータの起動前に実行されるウィザード。
 * 
 * @param <T> ウィザード後に起動するエクスポータの型
 * @param <C> ウィザード後に起動するエクスポータに対する設定を表す型
 * @author daisuke
 */
public interface ExporterWizard<T extends Exporter<C>, C extends ExportConfig> extends IWizard {
	
	/**
	 * 設定を取得する。
	 * 
	 * @return 設定
	 */
	C getConfig();
	
	/**
	 * エディタに設定されているEditorInputを与える。
	 * 
	 * @param input エディタに設定されているEditorInput
	 */
	void setInput(IFileEditorInput input);
}
