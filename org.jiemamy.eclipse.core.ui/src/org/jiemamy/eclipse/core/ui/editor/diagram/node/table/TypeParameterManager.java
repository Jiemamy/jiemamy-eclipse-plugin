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
package org.jiemamy.eclipse.core.ui.editor.diagram.node.table;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import org.jiemamy.dialect.Necessity;
import org.jiemamy.eclipse.core.ui.editor.diagram.EditListener;
import org.jiemamy.eclipse.core.ui.utils.SwtUtil;
import org.jiemamy.eclipse.core.ui.utils.TextSelectionAdapter;
import org.jiemamy.model.column.SimpleJmColumn;
import org.jiemamy.model.datatype.DataType;
import org.jiemamy.model.datatype.SimpleDataType;
import org.jiemamy.model.datatype.TypeParameterKey;
import org.jiemamy.model.domain.SimpleJmDomain.DomainType;
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
	 * データ型装飾アダプタに適したコントロールを生成する。
	 * 
	 * <p>元から存在したコントロールはすべて破棄される。</p>
	 * 
	 * @param column カラム
	 * @param keys データ型パラメータキー集合
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public void createTypeParameterControls(SimpleJmColumn column, Collection<TypeParameterKey<?>> keys) {
		Validate.notNull(column);
		Validate.noNullElements(keys);
		
		disposeTypeParameterControls();
		
		DataType dataType = column.getDataType();
		if (dataType.getRawTypeDescriptor() instanceof DomainType) {
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
			chkWithTimezone.addSelectionListener(editListener);
		}
		if (keys.contains(TypeParameterKey.SERIAL)) {
			chkSerial = new Button(composite, SWT.CHECK);
			chkSerial.setText("自動採番"); // RESOURCE
			chkSerial.addSelectionListener(editListener);
		}
		
		if (handler != null) {
			handler.createControl(column, composite, editListener);
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
	 * オプションコントロールを全て破棄する。
	 */
	public void disposeTypeParameterControls() {
		Control[] children = composite.getChildren();
		for (Control control : children) {
			control.dispose();
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
	 * コントロールからアダプタにデータを書き戻す。
	 * 
	 * @param column カラム
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public void setParametersFromControl(SimpleJmColumn column) {
		Validate.notNull(column);
		SimpleDataType dataType = (SimpleDataType) column.getDataType();
		if (dataType.getRawTypeDescriptor() instanceof DomainType) {
			return;
		}
		
		Map<TypeParameterKey<?>, Necessity> specs = dialect.getTypeParameterSpecs(dataType.getRawTypeDescriptor());
		Set<TypeParameterKey<?>> keys = specs.keySet();
		
		if (SwtUtil.isAlive(txtSize) && keys.contains(TypeParameterKey.SIZE)) {
			String text = txtSize.getText();
			if (StringUtils.isEmpty(text) == false) {
				Integer value = 1;
				try {
					value = Integer.valueOf(text);
					txtSize.setBackground(null);
				} catch (NumberFormatException e) {
					txtSize.setBackground(COLOR_ERROR);
				}
				if (value >= 1) {
					dataType.putParam(TypeParameterKey.SIZE, value);
				} else {
					txtSize.setBackground(COLOR_ERROR);
				}
			} else if (specs.get(TypeParameterKey.SIZE) == Necessity.REQUIRED) {
				txtSize.setBackground(COLOR_ERROR);
			} else {
				txtSize.setBackground(null);
				dataType.removeParam(TypeParameterKey.SIZE);
			}
		}
		if (SwtUtil.isAlive(txtPrecision) && keys.contains(TypeParameterKey.PRECISION)) {
			String text = txtPrecision.getText();
			if (StringUtils.isEmpty(text) == false) {
				Integer value = 1;
				try {
					value = Integer.valueOf(text);
					txtPrecision.setBackground(null);
				} catch (NumberFormatException e) {
					txtPrecision.setBackground(COLOR_ERROR);
				}
				if (value >= 1) {
					dataType.putParam(TypeParameterKey.PRECISION, value);
				} else {
					txtPrecision.setBackground(COLOR_ERROR);
				}
			} else if (specs.get(TypeParameterKey.PRECISION) == Necessity.REQUIRED) {
				txtPrecision.setBackground(COLOR_ERROR);
			} else {
				txtPrecision.setBackground(null);
				dataType.removeParam(TypeParameterKey.PRECISION);
			}
		}
		if (SwtUtil.isAlive(txtScale) && keys.contains(TypeParameterKey.SCALE)) {
			String text = txtScale.getText();
			if (StringUtils.isEmpty(text) == false) {
				Integer value = 0;
				try {
					value = Integer.valueOf(text);
					txtScale.setBackground(null);
				} catch (NumberFormatException e) {
					txtScale.setBackground(COLOR_ERROR);
				}
				if (value >= 0) {
					dataType.putParam(TypeParameterKey.SCALE, value);
				} else {
					txtPrecision.setBackground(COLOR_ERROR);
				}
			} else if (specs.get(TypeParameterKey.SCALE) == Necessity.REQUIRED) {
				txtScale.setBackground(COLOR_ERROR);
			} else {
				txtScale.setBackground(null);
				dataType.removeParam(TypeParameterKey.SCALE);
			}
		}
		if (SwtUtil.isAlive(chkWithTimezone) && keys.contains(TypeParameterKey.WITH_TIMEZONE)) {
			boolean value = chkWithTimezone.getSelection();
			if (specs.get(TypeParameterKey.WITH_TIMEZONE) == Necessity.REQUIRED || value == true) {
				dataType.putParam(TypeParameterKey.WITH_TIMEZONE, value);
			} else {
				dataType.removeParam(TypeParameterKey.WITH_TIMEZONE);
			}
		}
		if (SwtUtil.isAlive(chkSerial) && keys.contains(TypeParameterKey.SERIAL)) {
			boolean value = chkSerial.getSelection();
			if (specs.get(TypeParameterKey.SERIAL) == Necessity.REQUIRED || value == true) {
				dataType.putParam(TypeParameterKey.SERIAL, value);
			} else {
				dataType.removeParam(TypeParameterKey.SERIAL);
			}
		}
		if (handler != null) {
			handler.setParametersFromControl();
		}
		column.setDataType(dataType);
	}
	
	/**
	 * データ型からパラメータを読み出し、widgetに値を設定する。
	 * 
	 * @param dataType データ型
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public void setParametersToControl(DataType dataType) {
		Validate.notNull(dataType);
		if (dataType.getRawTypeDescriptor() instanceof DomainType) {
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
				handler.setParametersToControl();
			}
		}
	}
}
