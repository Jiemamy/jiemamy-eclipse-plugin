/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2011/01/19
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

import com.google.common.base.Function;

import org.jiemamy.dialect.TypeParameterSpec;
import org.jiemamy.model.datatype.TypeParameterKey;

/**
 * {@link TypeParameterSpec}を{@link TypeParameterKey}に変換する関数オブジェクト。
 * 
 * @version $Id$
 * @author daisuke
 */
public class SpecsToKeys implements Function<TypeParameterSpec, TypeParameterKey<?>> {
	
	/** singleton instance */
	public static final SpecsToKeys INSTANCE = new SpecsToKeys();
	

	private SpecsToKeys() {
	}
	
	public TypeParameterKey<?> apply(TypeParameterSpec spec) {
		return spec.getKey();
	}
}
