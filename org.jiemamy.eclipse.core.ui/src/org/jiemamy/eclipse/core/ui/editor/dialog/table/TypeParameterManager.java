/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2009/03/18
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
package org.jiemamy.eclipse.core.ui.editor.dialog.table;

import java.util.Collection;
import java.util.Map.Entry;

import com.google.common.collect.Collections2;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.dialect.Dialect;
import org.jiemamy.dialect.TypeParameterSpec;
import org.jiemamy.eclipse.core.ui.editor.dialog.EditListener;
import org.jiemamy.eclipse.core.ui.utils.SpecsToKeys;
import org.jiemamy.eclipse.core.ui.utils.SwtUtil;
import org.jiemamy.eclipse.core.ui.utils.TextSelectionAdapter;
import org.jiemamy.model.column.DefaultColumnModel;
import org.jiemamy.model.datatype.DefaultTypeVariant;
import org.jiemamy.model.datatype.TypeParameterKey;
import org.jiemamy.model.datatype.TypeVariant;
import org.jiemamy.model.domain.DefaultDomainModel.DomainType;
import org.jiemamy.utils.LogMarker;

/**
 * データ型変更時における、オプションコントロール（サイズ・精度・位取り等）の再構築を担うクラス。
 * 
 * TODO v0.2由来のよくわからんクラスなので、リファクタリングが必要。
 * 
 * @author daisuke
 */
class TypeParameterManager {
	
	private static Logger logger = LoggerFactory.getLogger(TypeParameterManager.class);
	
	/** エラーをあらわすUI色 */
	public static final Color COLOR_ERROR = new Color(null, 255, 200, 200);
	
	private final Dialect dialect;
	
	/** オプションコントロール描画対象の親 */
	private final Composite composite;
	
	/** コントロールの操作を検知するリスナ */
	private final EditListener editListener;
	
	private final TypeParameterHandler handler;
	
	/** サイズコントロール */
	private Text txtSize;
	
	/** 精度コントロール */
	private Text txtPrecision;
	
	/** 位取りコントロール */
	private Text txtScale;
	
	/** タイムゾーンコントロール */
	private Button chkWithTimezone;
	
	/** 自動採番コントロール */
	private Button chkSerial;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param dialect {@link Dialect}
	 * @param composite オプションコントロール描画対象の親
	 * @param editListener コントロールの操作を検知するリスナ
	 * @param handler may be null
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public TypeParameterManager(Dialect dialect, Composite composite, EditListener editListener,
			TypeParameterHandler handler) {
		Validate.notNull(dialect);
		Validate.notNull(composite);
		Validate.notNull(editListener);
		this.dialect = dialect;
		this.composite = composite;
		this.editListener = editListener;
		this.handler = handler;
	}
	
	/**
	 * オプションコントロールを全て破棄する。
	 */
	public void clearTypeOptionControl() {
		Control[] children = composite.getChildren();
		for (Control control : children) {
			control.dispose();
		}
	}
	
	/**
	 * データ型装飾アダプタに適したコントロールを生成する。
	 * 
	 * <p>元から存在したコントロールはすべて破棄される。</p>
	 * 
	 * @param columnModel 
	 * @param keys データ型パラメータキー集合
	 */
	public void createTypeOptionControl(DefaultColumnModel columnModel, Collection<TypeParameterKey<?>> keys) {
		clearTypeOptionControl();
		
		TypeVariant dataType = columnModel.getDataType();
		if (dataType.getTypeReference() instanceof DomainType) {
			return;
		}
		
		if (keys.contains(TypeParameterKey.SIZE)) {
			Label label = new Label(composite, SWT.NULL);
			label.setText("サイズ"); // RESOURCE
			
			txtSize = new Text(composite, SWT.BORDER);
			txtSize.addFocusListener(new TextSelectionAdapter(txtSize));
			txtSize.addKeyListener(editListener);
		}
		if (keys.contains(TypeParameterKey.PRECISION)) {
			Label label = new Label(composite, SWT.NULL);
			label.setText("精度"); // RESOURCE
			
			txtPrecision = new Text(composite, SWT.BORDER);
			txtPrecision.addFocusListener(new TextSelectionAdapter(txtPrecision));
			txtPrecision.addKeyListener(editListener);
		}
		if (keys.contains(TypeParameterKey.SCALE)) {
			Label label = new Label(composite, SWT.NULL);
			label.setText("スケール"); // RESOURCE
			
			txtScale = new Text(composite, SWT.BORDER);
			txtScale.addFocusListener(new TextSelectionAdapter(txtScale));
			txtScale.addKeyListener(editListener);
		}
		if (keys.contains(TypeParameterKey.WITH_TIMEZONE)) {
			chkWithTimezone = new Button(composite, SWT.CHECK);
			chkWithTimezone.setText("WITH TIMEZONE"); // RESOURCE
			chkWithTimezone.addKeyListener(editListener);
		}
		if (keys.contains(TypeParameterKey.SERIAL)) {
			chkSerial = new Button(composite, SWT.CHECK);
			chkSerial.setText("自動採番"); // RESOURCE
			chkSerial.addSelectionListener(editListener);
		}
		
		if (handler != null) {
			handler.createControl(columnModel, composite, editListener);
		}
		
		// HACK 再描画されない問題への暫定解決策。
		// Swingでのバッドノウハウをそのまま継承して試したところ、あっさり動作したが、BKはBK。
		// なんとかしたいが、なんともならない。こんな実装したくないとか言う以前に他の解決策が見つからない。
		// 1. キャッシュ？のクリア
		//   Composite#changed(Control[])が怪しいと思うけど、怪しいだけであんまり関係ないかも。
		// 2. Widget#setBounds()の最後に以下のような実装がある。
		//      int result = 0;
		//      if (move && !sameOrigin) {
		//        if (events) sendEvent (SWT.Move);
		//        result |= MOVED;
		//	    }
		//	    if (resize && !sameExtent) {
		//	      if (events) sendEvent (SWT.Resize);
		//	      result |= RESIZED;
		//	    }
		//   移動したか、サイズが変わったか？を判断し、イベントを投げている。
		//   今回のBKでは「サイズが変わった」を実際に実施しているため、うまく動作するだけ。
		//   無理矢理 sendEvent(SWT.Resize)する方がマシだが、どこに向かって投げるべきか？
		//   とかを調査してなんとかしたいとこだが、shin1ogawaにはわかんないので何ともならないし、
		//   今日は追っかける気力が無いのでこのままcommitする。
		//   ちなみに、幅を-1しているが、pack()すると元に戻るので後調整は必要無い。
		Rectangle bounds = composite.getBounds();
		composite.setBounds(bounds.x, bounds.y, bounds.width - 1, bounds.height);
		
		logger.debug(LogMarker.DETAIL, "before pack: " + composite.getSize());
		composite.pack(true);
		logger.debug(LogMarker.DETAIL, "after pack: " + composite.getSize());
	}
	
	/**
	 * オプションコントロールをすべて無効にする。
	 */
	public void disable() {
		SwtUtil.setEnabledIfAlive(txtSize, false);
		SwtUtil.setEnabledIfAlive(txtPrecision, false);
		SwtUtil.setEnabledIfAlive(txtScale, false);
		SwtUtil.setEnabledIfAlive(chkWithTimezone, false);
		SwtUtil.setEnabledIfAlive(chkSerial, false);
		if (handler != null) {
			handler.disable();
		}
	}
	
	/**
	 * オプションコントロールをすべて有効にする。
	 */
	public void enable() {
		SwtUtil.setEnabledIfAlive(txtSize, true);
		SwtUtil.setEnabledIfAlive(txtPrecision, true);
		SwtUtil.setEnabledIfAlive(txtScale, true);
		SwtUtil.setEnabledIfAlive(chkWithTimezone, true);
		SwtUtil.setEnabledIfAlive(chkSerial, true);
		if (handler != null) {
			handler.enable();
		}
	}
	
	/**
	 * アダプタからコントロールに値を格納する。
	 * 
	 * @param columnModel 
	 */
	public void setValue(DefaultColumnModel columnModel) {
		TypeVariant dataType = columnModel.getDataType();
		if (dataType.getTypeReference() instanceof DomainType) {
			return;
		}
		
		for (Entry<String, String> entry : dataType.getParams()) {
			if (entry.getKey().equals(TypeParameterKey.SIZE.getKeyString())) {
				Integer size = dataType.getParam(TypeParameterKey.SIZE);
				txtSize.setText(StringUtils.defaultString(ObjectUtils.toString(size)));
			}
			if (entry.getKey().equals(TypeParameterKey.PRECISION.getKeyString())) {
				Integer precision = dataType.getParam(TypeParameterKey.PRECISION);
				txtPrecision.setText(StringUtils.defaultString(ObjectUtils.toString(precision)));
			}
			if (entry.getKey().equals(TypeParameterKey.SCALE.getKeyString())) {
				Integer scale = dataType.getParam(TypeParameterKey.SCALE);
				txtScale.setText(StringUtils.defaultString(ObjectUtils.toString(scale)));
			}
			if (entry.getKey().equals(TypeParameterKey.WITH_TIMEZONE.getKeyString())) {
				Boolean withTimeZone = dataType.getParam(TypeParameterKey.WITH_TIMEZONE);
				chkWithTimezone.setSelection(BooleanUtils.isTrue(withTimeZone));
			}
			if (entry.getKey().equals(TypeParameterKey.SERIAL.getKeyString())) {
				Boolean serial = dataType.getParam(TypeParameterKey.SERIAL);
				chkSerial.setSelection(BooleanUtils.isTrue(serial));
			}
			if (handler != null) {
				handler.setValue();
			}
		}
	}
	
	/**
	 * コントロールからアダプタにデータを書き戻す。
	 */
	public void writeBackToAdapter(DefaultColumnModel columnModel) {
		DefaultTypeVariant dataType = (DefaultTypeVariant) columnModel.getDataType();
		if (dataType.getTypeReference() instanceof DomainType) {
			return;
		}
		
		Collection<TypeParameterSpec> specs = dialect.getTypeParameterSpecs(dataType.getTypeReference());
		Collection<TypeParameterKey<?>> keys = Collections2.transform(specs, SpecsToKeys.INSTANCE);
		
		if (SwtUtil.isAlive(txtSize) && keys.contains(TypeParameterKey.SIZE)) {
			String text = txtSize.getText();
			Integer value = 0;
			try {
				value = Integer.valueOf(text);
				txtSize.setBackground(null);
			} catch (NumberFormatException e) {
				txtSize.setBackground(COLOR_ERROR);
			}
			dataType.putParam(TypeParameterKey.SIZE, value);
		}
		if (SwtUtil.isAlive(txtPrecision) && keys.contains(TypeParameterKey.PRECISION)) {
			String text = txtPrecision.getText();
			Integer value = 0;
			try {
				value = Integer.valueOf(text);
				txtPrecision.setBackground(null);
			} catch (NumberFormatException e) {
				txtPrecision.setBackground(COLOR_ERROR);
			}
			dataType.putParam(TypeParameterKey.PRECISION, value);
		}
		if (SwtUtil.isAlive(txtScale) && keys.contains(TypeParameterKey.SCALE)) {
			String text = txtScale.getText();
			Integer value = 0;
			if (StringUtils.isEmpty(text)) {
				txtScale.setBackground(null);
			} else {
				try {
					value = Integer.valueOf(text);
					txtScale.setBackground(null);
				} catch (NumberFormatException e) {
					txtScale.setBackground(COLOR_ERROR);
				}
			}
			dataType.putParam(TypeParameterKey.SCALE, value);
		}
		if (SwtUtil.isAlive(chkWithTimezone) && keys.contains(TypeParameterKey.WITH_TIMEZONE)) {
			boolean value = chkWithTimezone.getSelection();
			dataType.putParam(TypeParameterKey.WITH_TIMEZONE, value);
		}
		if (SwtUtil.isAlive(chkSerial) && keys.contains(TypeParameterKey.SERIAL)) {
			boolean value = chkSerial.getSelection();
			dataType.putParam(TypeParameterKey.SERIAL, value);
		}
		if (handler != null) {
			handler.writeBackToAdapter();
		}
		columnModel.setDataType(dataType);
	}
}
