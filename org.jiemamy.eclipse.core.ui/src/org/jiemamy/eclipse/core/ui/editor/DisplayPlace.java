/*
 * Copyright 2007-2012 Jiemamy Project and the Others.
 * Created on 2008/08/02
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

/**
 * 文字列の表示場所（用途）を表す。
 * 
 * @author daisuke
 */
public enum DisplayPlace {
	
	/** ダイアグラム上のFigureに表示することを表す */
	FIGURE,
	
	/** アウトラインツリー上に表示する事を表す */
	OUTLINE_TREE,
	
	/** 表中に表示することを表す */
	TABLE;
}
