/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
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

import java.util.Collection;

import org.jiemamy.dialect.Dialect;
import org.jiemamy.dialect.TypeParameterSpec;
import org.jiemamy.dialect.TypeParameterSpec.Necessity;
import org.jiemamy.eclipse.core.ui.editor.DisplayPlace;
import org.jiemamy.model.datatype.TypeParameterKey;
import org.jiemamy.model.datatype.DataType;

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
	 * @param place 表示しようと考えている場所
	 * @return 表示用文字列
	 */
	public static String toString(Dialect dialect, DataType dataType, DisplayPlace place) {
		StringBuilder typeName = new StringBuilder(dataType.getTypeReference().getTypeName());
		String suffix = "";
		
		Integer size = null;
		Integer precision = null;
		Integer scale = null;
		
		Collection<TypeParameterSpec> specs = dialect.getTypeParameterSpecs(dataType.getTypeReference());
		for (TypeParameterSpec spec : specs) {
			if (spec.getNecessity() == Necessity.REQUIRED) {
				if (spec.getKey().equals(TypeParameterKey.SERIAL)) {
					typeName.insert(0, "SERIAL ");
				} else if (spec.getKey().equals(TypeParameterKey.WITH_TIMEZONE)) {
					suffix = " WITH TIMEZONE";
				} else if (spec.getKey().equals(TypeParameterKey.SIZE)) {
					size = dataType.getParam(TypeParameterKey.SIZE);
				} else if (spec.getKey().equals(TypeParameterKey.PRECISION)) {
					precision = dataType.getParam(TypeParameterKey.PRECISION);
				} else if (spec.getKey().equals(TypeParameterKey.SCALE)) {
					scale = dataType.getParam(TypeParameterKey.SCALE);
				}
			}
		}
		
		if (size != null) {
			typeName.append("(").append(size).append(")");
		}
		if (precision != null && scale != null) {
			typeName.append("(").append(precision).append(", ").append(scale).append(")");
		}
		
		return typeName.append(suffix).toString();
	}
	
	private LabelStringUtil() {
	}
}
