/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
 * Created on 2008/08/03
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
package org.jiemamy.eclipse.editor;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.Validate;

import org.jiemamy.JiemamyContext;
import org.jiemamy.JiemamyEntity;
import org.jiemamy.dddbase.AbstractEntity;
import org.jiemamy.dddbase.Entity;
import org.jiemamy.dddbase.EntityRef;
import org.jiemamy.serializer.JiemamyXmlWriter;

/**
 * 分類コンテナ。アウトラインツリー用。
 * 
 * @author daisuke
 */
public class ModelContainer extends AbstractEntity implements JiemamyEntity {
	
	/** コンテナID */
	private final String containerId;
	
	/** コンテナ名 */
	private final String name;
	
	/** コンテナが持つ子モデル */
	private List<Object> children = Lists.newArrayList();
	
	private final UUID id;
	
	private final JiemamyContext context;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param containerId コンテナID
	 * @param name 作成するコンテナの名前
	 * @param context コンテキスト
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public ModelContainer(String containerId, String name, JiemamyContext context) {
		super(UUID.randomUUID());
		Validate.notNull(containerId);
		Validate.notNull(name);
		Validate.notNull(context);
		this.containerId = containerId;
		this.name = name;
		this.context = context;
		id = UUID.randomUUID();
	}
	
	/**
	 * childrenを取得する。
	 * 
	 * @return children
	 */
	public List<Object> getChildren() {
		return children;
	}
	
	/**
	 *  コンテナIDを取得する。
	 *  
	 * @return  コンテナID
	 */
	public String getContainerId() {
		return containerId;
	}
	
	public JiemamyContext getJiemamy() {
		return context;
	}
	
	/**
	 * コンテナ名を取得する。
	 * 
	 * @return コンテナ名
	 */
	public String getName() {
		return name;
	}
	
	public JiemamyXmlWriter getWriter(JiemamyContext context) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * コンテナが持つ子モデルを設定する。
	 * 
	 * @param children コンテナが持つ子モデル
	 */
	public void setChildren(List<Object> children) {
		this.children = children;
	}
	
	public EntityRef<? extends Entity> toReference() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String toString() {
		return ClassUtils.getShortClassName(this.getClass()) + "[" + name + "]";
	}
}
