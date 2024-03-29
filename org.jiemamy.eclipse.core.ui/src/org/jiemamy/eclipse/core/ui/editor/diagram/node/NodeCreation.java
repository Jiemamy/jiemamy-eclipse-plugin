/*
 * Copyright 2007-2012 Jiemamy Project and the Others.
 * Created on 2010/12/28
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
package org.jiemamy.eclipse.core.ui.editor.diagram.node;

import org.jiemamy.eclipse.core.ui.editor.diagram.Creation;
import org.jiemamy.model.geometory.JmRectangle;

/**
 * ノードとその関連モデルの生成を表すインターフェイス。
 * 
 * @version $Id$
 * @author daisuke
 */
public interface NodeCreation extends Creation {
	
	/**
	 * ノードの位置サイズを設定する。
	 * 
	 * @param boundary ノードの位置サイズ
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	void setBoundary(JmRectangle boundary);
	
}
