/*
 * Copyright 2007-2009 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.editor.dialog;

import java.util.Collection;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.eclipse.ui.UIConstant;
import org.jiemamy.eclipse.ui.helper.TextSelectionAdapter;
import org.jiemamy.eclipse.utils.ExceptionHandler;
import org.jiemamy.eclipse.utils.SwtUtil;
import org.jiemamy.model.attribute.ColumnModel;
import org.jiemamy.model.dbo.DomainModel;
import org.jiemamy.utils.LogMarker;

/**
 * データ型変更時における、オプションコントロール（サイズ・精度・位取り等）の再構築を担うクラス。
 * 
 * @author daisuke
 */
public class TypeOptionManager {
	
	private static Logger logger = LoggerFactory.getLogger(TypeOptionManager.class);
	
	private final DataTypeHolder<? extends DataType> holder;
	
	/** オプションコントロール描画対象の親 */
	private final Composite composite;
	
	/** コントロールの操作を検知するリスナ */
	private final EditListener editListener;
	
	private final TypeOptionHandler handler;
	
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
	 * @param holder データ型を設定されるモデル（{@link ColumnModel}または{@link DomainModel}）
	 * @param composite オプションコントロール描画対象の親
	 * @param editListener コントロールの操作を検知するリスナ
	 * @param handler may be null
	 * @throws IllegalArgumentException 引数に{@code null}を与えた場合
	 */
	public TypeOptionManager(DataTypeHolder<? extends DataType> holder, Composite composite, EditListener editListener,
			TypeOptionHandler handler) {
		Validate.notNull(holder);
		Validate.notNull(composite);
		Validate.notNull(editListener);
		this.holder = holder;
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
	 * @param adapterClasses データ型装飾アダプタの集合
	 */
	public void createTypeOptionControl(Collection<Class<?>> adapterClasses) {
		clearTypeOptionControl();
		
		DataType dataType = holder.getDataType();
		if ((dataType instanceof BuiltinDataType) == false) {
			return;
		}
		BuiltinDataType builtinDataType = (BuiltinDataType) dataType;
		for (Class<?> clazz : adapterClasses) {
			if (builtinDataType.hasAdapter(clazz) == false) {
				try {
					builtinDataType.registerAdapter(clazz.newInstance());
				} catch (InstantiationException e) {
					ExceptionHandler.handleException(e);
				} catch (IllegalAccessException e) {
					ExceptionHandler.handleException(e);
				}
			}
		}
		
		if (builtinDataType.hasAdapter(SizedDataTypeAdapter.class)) {
			Label label = new Label(composite, SWT.NULL);
			label.setText("サイズ"); // RESOURCE
			
			txtSize = new Text(composite, SWT.BORDER);
			txtSize.addFocusListener(new TextSelectionAdapter(txtSize));
			txtSize.addKeyListener(editListener);
		}
		if (builtinDataType.hasAdapter(PrecisionedDataTypeAdapter.class)) {
			Label label = new Label(composite, SWT.NULL);
			label.setText("精度"); // RESOURCE
			
			txtPrecision = new Text(composite, SWT.BORDER);
			txtPrecision.addFocusListener(new TextSelectionAdapter(txtPrecision));
			txtPrecision.addKeyListener(editListener);
			
			label = new Label(composite, SWT.NULL);
			label.setText("位取り"); // RESOURCE
			
			txtScale = new Text(composite, SWT.BORDER);
			txtScale.addFocusListener(new TextSelectionAdapter(txtScale));
			txtScale.addKeyListener(editListener);
		}
		if (builtinDataType.hasAdapter(TimezonedDataTypeAdapter.class)) {
			chkWithTimezone = new Button(composite, SWT.CHECK);
			chkWithTimezone.setText("WITH TIMEZONE"); // RESOURCE
			chkWithTimezone.addKeyListener(editListener);
		}
		if (builtinDataType.hasAdapter(SerialDataTypeAdapter.class)) {
			chkSerial = new Button(composite, SWT.CHECK);
			chkSerial.setText("自動採番"); // RESOURCE
			chkSerial.addSelectionListener(editListener);
		}
		
		if (handler != null) {
			handler.createControl(holder, composite, editListener);
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
	 * @param clazz 対象アダプタ
	 */
	public void setValue(Class<?> clazz) {
		DataType dataType = holder.getDataType();
		if ((dataType instanceof BuiltinDataType) == false) {
			return;
		}
		BuiltinDataType builtinDataType = (BuiltinDataType) dataType;
		
		for (Object adapter : builtinDataType.getAdapters()) {
			if (adapter instanceof SizedDataTypeAdapter) {
				SizedDataTypeAdapter sizedDataTypeAdapter = (SizedDataTypeAdapter) adapter;
				Integer size = sizedDataTypeAdapter.getSize();
				txtSize.setText(JiemamyPropertyUtil.careNull(ObjectUtils.toString(size)));
			}
			if (adapter instanceof PrecisionedDataTypeAdapter) {
				PrecisionedDataTypeAdapter precisionedDataTypeAdapter = (PrecisionedDataTypeAdapter) adapter;
				Integer precision = precisionedDataTypeAdapter.getPrecision();
				Integer scale = precisionedDataTypeAdapter.getScale();
				txtPrecision.setText(JiemamyPropertyUtil.careNull(ObjectUtils.toString(precision)));
				txtScale.setText(JiemamyPropertyUtil.careNull(ObjectUtils.toString(scale)));
			}
			if (adapter instanceof TimezonedDataTypeAdapter) {
				TimezonedDataTypeAdapter timezonedDataTypeAdapter = (TimezonedDataTypeAdapter) adapter;
				Boolean withTimeZone = timezonedDataTypeAdapter.isWithTimezone();
				chkWithTimezone.setSelection(BooleanUtils.isTrue(withTimeZone));
			}
			if (adapter instanceof SerialDataTypeAdapter) {
				SerialDataTypeAdapter serialDataTypeAdapter = (SerialDataTypeAdapter) adapter;
				Boolean serial = serialDataTypeAdapter.isSerial();
				chkSerial.setSelection(BooleanUtils.isTrue(serial));
			}
			if (handler != null) {
				handler.setValue(clazz);
			}
		}
	}
	
	/**
	 * コントロールからアダプタにデータを書き戻す。
	 */
	public void writeBackToAdapter() {
		DataType dataType = holder.getDataType();
		if ((dataType instanceof BuiltinDataType) == false) {
			return;
		}
		BuiltinDataType builtinDataType = (BuiltinDataType) dataType;
		
		if (SwtUtil.isAlive(txtSize) && builtinDataType.hasAdapter(SizedDataTypeAdapter.class)) {
			String text = txtSize.getText();
			Integer value = null;
			try {
				value = Integer.valueOf(text);
				txtSize.setBackground(null);
			} catch (NumberFormatException e) {
				txtSize.setBackground(UIConstant.COLOR_ERROR);
			}
			builtinDataType.getAdapter(SizedDataTypeAdapter.class).setSize(value);
		}
		if (SwtUtil.isAlive(txtPrecision) && builtinDataType.hasAdapter(PrecisionedDataTypeAdapter.class)) {
			String text = txtPrecision.getText();
			Integer value = null;
			try {
				value = Integer.valueOf(text);
				txtPrecision.setBackground(null);
			} catch (NumberFormatException e) {
				txtPrecision.setBackground(UIConstant.COLOR_ERROR);
			}
			builtinDataType.getAdapter(PrecisionedDataTypeAdapter.class).setPrecision(value);
		}
		if (SwtUtil.isAlive(txtScale) && builtinDataType.hasAdapter(PrecisionedDataTypeAdapter.class)) {
			String text = txtScale.getText();
			Integer value = null;
			if (StringUtils.isEmpty(text)) {
				txtScale.setBackground(null);
			} else {
				try {
					value = Integer.valueOf(text);
					txtScale.setBackground(null);
				} catch (NumberFormatException e) {
					txtScale.setBackground(UIConstant.COLOR_ERROR);
				}
			}
			builtinDataType.getAdapter(PrecisionedDataTypeAdapter.class).setScale(value);
		}
		if (SwtUtil.isAlive(chkWithTimezone) && builtinDataType.hasAdapter(TimezonedDataTypeAdapter.class)) {
			boolean value = chkWithTimezone.getSelection();
			builtinDataType.getAdapter(TimezonedDataTypeAdapter.class).setWithTimezone(value == false ? null : value);
		}
		if (SwtUtil.isAlive(chkSerial) && builtinDataType.hasAdapter(SerialDataTypeAdapter.class)) {
			boolean value = chkSerial.getSelection();
			builtinDataType.getAdapter(SerialDataTypeAdapter.class).setSerial(value == false ? null : value);
		}
		if (handler != null) {
			handler.writeBackToAdapter();
		}
	}
	
}
