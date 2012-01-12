/*
 * Copyright 2007-2012 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.core.ui.utils;

import java.util.Map;

import org.apache.commons.lang.Validate;

import org.jiemamy.dialect.Dialect;
import org.jiemamy.dialect.Necessity;
import org.jiemamy.model.datatype.DataType;
import org.jiemamy.model.datatype.TypeParameterKey;

/**
 * UI表示用文字列を生成するユーティリティクラス。
 * 
 * @author daisuke
 */
public class LabelStringUtil {
	
	/**
	 * DataTypeに対する表示用文字列を取得する。
	 * 
	 * @param dialect {@link Dialect}
	 * @param dataType 表示対象DataType
	 * @return 表示用文字列
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public static String toString(Dialect dialect, DataType dataType) {
		Validate.notNull(dialect);
		Validate.notNull(dataType);
		StringBuilder typeName = new StringBuilder(dataType.getRawTypeDescriptor().getTypeName());
		String suffix = "";
		
		Integer size = null;
		Integer precision = null;
		Integer scale = null;
		
		Map<TypeParameterKey<?>, Necessity> specs = dialect.getTypeParameterSpecs(dataType.getRawTypeDescriptor());
		for (Map.Entry<TypeParameterKey<?>, Necessity> entry : specs.entrySet()) {
			if (entry.getKey().equals(TypeParameterKey.SERIAL)) {
				Boolean serial = dataType.getParam(TypeParameterKey.SERIAL);
				if (serial != null && serial) {
					typeName.insert(0, "SERIAL ");
				}
			} else if (entry.getKey().equals(TypeParameterKey.WITH_TIMEZONE)) {
				Boolean withTimeZone = dataType.getParam(TypeParameterKey.WITH_TIMEZONE);
				if (withTimeZone != null && withTimeZone) {
					suffix = " WITH TIMEZONE";
				}
			} else if (entry.getKey().equals(TypeParameterKey.SIZE)) {
				size = dataType.getParam(TypeParameterKey.SIZE);
			} else if (entry.getKey().equals(TypeParameterKey.PRECISION)) {
				precision = dataType.getParam(TypeParameterKey.PRECISION);
			} else if (entry.getKey().equals(TypeParameterKey.SCALE)) {
				scale = dataType.getParam(TypeParameterKey.SCALE);
			}
		}
		
		if (size != null) {
			typeName.append("(").append(size).append(")");
		} else if (precision != null && scale != null) {
			typeName.append("(").append(precision).append(", ").append(scale).append(")");
		}
		
		return typeName.append(suffix).toString();
	}
	
	private LabelStringUtil() {
	}
}
