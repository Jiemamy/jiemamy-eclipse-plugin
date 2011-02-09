/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2011/01/17
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
package org.jiemamy.eclipse.core.ui.utils;

import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import org.apache.commons.lang.Validate;

import org.jiemamy.JiemamyContext;
import org.jiemamy.dddbase.EntityNotFoundException;
import org.jiemamy.dddbase.EntityRef;
import org.jiemamy.model.column.JmColumn;
import org.jiemamy.model.constraint.JmKeyConstraint;

/**
 * {@link JmKeyConstraint}に関するユーティリティクラス。
 * 
 * @version $Id$
 * @author daisuke
 */
public final class KeyConstraintUtil {
	
	/**
	 * キーカラム名のカンマ区切りの文字列に変換する。
	 * 
	 * @param context コンテキスト
	 * @param keyConstraint 変換対象キー
	 * @return キーカラム名のカンマ区切りの文字列
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public static String toStringKeyColumns(JiemamyContext context, JmKeyConstraint keyConstraint) {
		Validate.notNull(context);
		Validate.notNull(keyConstraint);
		
		List<EntityRef<? extends JmColumn>> keyColumns = keyConstraint.getKeyColumns();
		List<String> columnNames = Lists.newArrayListWithCapacity(keyColumns.size());
		for (EntityRef<? extends JmColumn> columnRef : keyColumns) {
			try {
				JmColumn col = context.resolve(columnRef);
				columnNames.add(col.getName());
			} catch (EntityNotFoundException e) {
				// FIXME tableにaddしたが、そのtableをstoreしていない状態では、
				// contextから該当columnresolveできないので、カラム名が分からない
				columnNames.add("<NewColumn>");
			}
		}
		return Joiner.on(", ").join(columnNames);
	}
	
	private KeyConstraintUtil() {
	}
	
}
