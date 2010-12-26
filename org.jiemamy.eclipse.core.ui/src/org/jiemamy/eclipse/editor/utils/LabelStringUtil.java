/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
 * Created on 2009/02/16
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
package org.jiemamy.eclipse.editor.utils;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.JiemamyContext;
import org.jiemamy.JiemamyEntity;
import org.jiemamy.dddbase.EntityRef;
import org.jiemamy.dialect.Dialect;
import org.jiemamy.eclipse.editor.DisplayPlace;
import org.jiemamy.model.attribute.ColumnModel;
import org.jiemamy.model.attribute.constraint.ForeignKeyConstraintModel;
import org.jiemamy.model.attribute.constraint.PrimaryKeyConstraintModel;
import org.jiemamy.model.dbo.DatabaseObjectModel;
import org.jiemamy.model.dbo.DomainModel;
import org.jiemamy.model.sql.DefaultSqlFormatter;
import org.jiemamy.model.sql.Separator;
import org.jiemamy.model.sql.Token;

/**
 * UI表示用文字列を生成するユーティリティクラス。
 * 
 * @author daisuke
 */
public class LabelStringUtil {
	
	private static Logger logger = LoggerFactory.getLogger(LabelStringUtil.class);
	

	/**
	 * DataTypeに対する表示用文字列を取得する。
	 * 
	 * @param rootModel ルートモデル
	 * @param dataType 表示対象DataType
	 * @param place 表示しようと考えている場所
	 * @return 表示用文字列
	 */
	public static String getString(JiemamyContext rootModel, DataType dataType, DisplayPlace place) {
		ReferenceResolver resolver = rootModel.getJiemamy().getReferenceResolver();
		try {
			Dialect dialect = rootModel.findDialect();
			List<Token> tokens = dialect.getDataTypeResolver().resolveDataType(dataType, resolver);
			StringBuilder sb = new StringBuilder();
			Token lastToken = null;
			for (Token token : tokens) {
				if ((DefaultSqlFormatter.isSeparator(lastToken) == false && DefaultSqlFormatter.isSeparator(token) == false)
						|| lastToken == null || lastToken.equals(Separator.COMMA)) {
					sb.append(DefaultSqlFormatter.WHITESPACE);
				}
				sb.append(token);
				lastToken = token;
			}
			return sb.toString();
		} catch (ClassNotFoundException e) {
			logger.warn("Dialectのロスト", e);
			return dataType.toBuiltinDataType(resolver).getTypeName();
		}
	}
	
	/**
	 * JiemamyElementに対する表示用文字列を取得する。
	 * 
	 * @param context ルートモデル
	 * @param targetElement 表示対象JiemamyElement
	 * @param place 表示しようと考えている場所
	 * @return 表示用文字列
	 */
	public static String getString(JiemamyContext context, JiemamyEntity targetElement, DisplayPlace place) {
		if (targetElement instanceof DomainModel) {
			DomainModel domainModel = (DomainModel) targetElement;
			return domainModel.getName();
		} else if (targetElement instanceof DatabaseObjectModel) {
			DatabaseObjectModel entityModel = (DatabaseObjectModel) targetElement;
			return entityModel.getName();
		} else if (targetElement instanceof ColumnModel) {
			ColumnModel columnModel = (ColumnModel) targetElement;
			return columnModel.getName();
		} else if (targetElement instanceof PrimaryKeyConstraintModel) {
			PrimaryKeyConstraintModel primaryKey = (PrimaryKeyConstraintModel) targetElement;
			StringBuilder sb = new StringBuilder("PK ");
			if (primaryKey.getName() != null) {
				sb.append(" ");
				sb.append(primaryKey.getName());
			}
			sb.append("(");
			sb.append(KeyConstraintUtil.toStringKeyColumns(primaryKey));
			sb.append(")");
			return sb.toString();
		} else if (targetElement instanceof ForeignKeyConstraintModel) {
			ForeignKeyConstraintModel foreignKey = (ForeignKeyConstraintModel) targetElement;
			StringBuilder sb = new StringBuilder();
			
			if (foreignKey.getName() != null) {
				sb.append(foreignKey.getName()).append("\n");
			}
			
			int size = Math.max(foreignKey.getReferenceColumns().size(), foreignKey.getKeyColumns().size());
			for (int i = 0; i < size; i++) {
				if (i != 0) {
					sb.append("\n");
				}
				if (foreignKey.getKeyColumns().size() > i) {
					EntityRef<? extends ColumnModel> keyColumnRef = foreignKey.getKeyColumns().get(i);
					ColumnModel keyColumn = context.resolve(keyColumnRef);
					sb.append(keyColumn.getName());
				} else {
					sb.append("UNKNOWN");
				}
				sb.append(" -> ");
				if (foreignKey.getReferenceColumns().size() > i) {
					EntityRef<? extends ColumnModel> referenceColumnRef = foreignKey.getReferenceColumns().get(i);
					ColumnModel referenceColumn = context.resolve(referenceColumnRef);
					sb.append(referenceColumn.getName());
				} else {
					sb.append("UNKNOWN");
				}
			}
			
			return sb.toString();
		}
		return "unknown label: " + targetElement.getClass().getName();
	}
	
	private LabelStringUtil() {
	}
}
