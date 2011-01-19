/*
 * Copyright 2007-2010 Jiemamy Project and the Others.
 * Created on 2010/12/14
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
package org.jiemamy.eclipse.core.ui;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import org.jiemamy.dialect.Dialect;
import org.jiemamy.dialect.TypeParameterSpec;
import org.jiemamy.model.datatype.DataTypeCategory;
import org.jiemamy.model.datatype.DefaultTypeReference;
import org.jiemamy.model.datatype.TypeReference;
import org.jiemamy.validator.CompositeValidator;
import org.jiemamy.validator.Validator;

/**
 * テスト用のモックSQL方言実装クラス。
 * 
 * @version $Id$
 * @author daisuke
 */
public class MockDialect implements Dialect {
	
	private List<TypeReference> allDataTypes = Lists.newArrayList();
	
	private Validator validator = new CompositeValidator();
	

	/**
	 * インスタンスを生成する。
	 */
	public MockDialect() {
		allDataTypes.add(new DefaultTypeReference(DataTypeCategory.INTEGER));
		allDataTypes.add(new DefaultTypeReference(DataTypeCategory.VARCHAR));
	}
	
	public List<TypeReference> getAllTypeReferences() {
		return allDataTypes;
	}
	
	public String getConnectionUriTemplate() {
		return "jdbc:dummy:foobar";
	}
	
	public String getName() {
		return this.getClass().getName();
	}
	
	public Collection<TypeParameterSpec> getTypeParameterSpecs(TypeReference reference) {
		return Collections.emptyList();
	}
	
	public Validator getValidator() {
		return validator;
	}
	
}
