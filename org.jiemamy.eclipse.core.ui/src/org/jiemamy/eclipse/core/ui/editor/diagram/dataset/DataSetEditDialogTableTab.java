/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2009/03/17
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
package org.jiemamy.eclipse.core.ui.editor.diagram.dataset;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.dddbase.EntityRef;
import org.jiemamy.eclipse.core.ui.editor.diagram.AbstractTab;
import org.jiemamy.model.column.JmColumn;
import org.jiemamy.model.dataset.JmDataSet;
import org.jiemamy.model.dataset.JmRecord;
import org.jiemamy.model.dataset.SimpleJmDataSet;
import org.jiemamy.model.dataset.SimpleJmRecord;
import org.jiemamy.model.table.JmTable;
import org.jiemamy.script.ScriptString;
import org.jiemamy.utils.LogMarker;

/**
 * データセット編集ダイアログの各テーブルのタブ。
 * 
 * @author daisuke
 */
public class DataSetEditDialogTableTab extends AbstractTab {
	
	private static final int COL_WIDTH = 100;
	
	private static Logger logger = LoggerFactory.getLogger(DataSetEditDialogTableTab.class);
	
	// TODO 123って適当なｗ
	/** 外部の操作によってレコードが変更されたことを通知するイベントを表すコード */
	public static final int RECORD_CHANGED = 123;
	
	private final Table swtTable;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param parentTabFolder 親となるタブフォルダ
	 * @param style SWTスタイル値
	 * @param dataSet 編集対象{@link SimpleJmDataSet}
	 * @param table {@link SimpleJmDataSet}内での対象テーブル
	 */
	public DataSetEditDialogTableTab(TabFolder parentTabFolder, int style, SimpleJmDataSet dataSet, JmTable table) {
		super(parentTabFolder, style, table.getName());
		getTabItem().setData(table);
		
		Composite composite = new Composite(parentTabFolder, SWT.NULL);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		swtTable = new Table(composite, SWT.BORDER | SWT.MULTI);
		swtTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		swtTable.setHeaderVisible(true);
		swtTable.setLinesVisible(true);
		swtTable.setData(table);
		
		List<JmColumn> columns = table.getColumns();
		for (JmColumn column : columns) {
			TableColumn swtColumn = new TableColumn(swtTable, SWT.NONE);
			swtColumn.setWidth(COL_WIDTH);
			swtColumn.setText(column.getName());
			swtColumn.setData(column);
		}
		
		final List<JmRecord> records = dataSet.getRecords().get(table.toReference());
		refreshTable(swtTable, records);
		
		final TableEditor editor = new TableEditor(swtTable);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		swtTable.addListener(SWT.MouseDown, new StartEditListener(editor));
		
		getTabItem().setControl(composite);
		getTabItem().addListener(RECORD_CHANGED, new Listener() {
			
			public void handleEvent(Event event) {
				refreshTable(swtTable, records);
			}
		});
	}
	
	@Override
	public boolean isTabComplete() {
		return true;
	}
	
	Table getSwtTable() {
		return swtTable;
	}
	
	private void refreshTable(final Table swtTable, List<JmRecord> records) {
		for (TableItem item : swtTable.getItems()) {
			item.dispose();
		}
		
		for (JmRecord record : records) {
			Map<EntityRef<? extends JmColumn>, ScriptString> values = record.getValues();
			List<String> data = Lists.newArrayList();
			for (TableColumn tableColumn : swtTable.getColumns()) {
				JmColumn column = (JmColumn) tableColumn.getData();
				EntityRef<? extends JmColumn> columnRef = column.toReference();
				
				// TODO nullケース、不在ケース、空文字ケースの区別をつける
				if (values.containsKey(columnRef)) {
					ScriptString string = values.get(columnRef);
					if (string == null) {
						data.add(""); // NULLケース
					} else {
						data.add(string.getScript());
					}
				} else {
					data.add(""); // 不在ケース
				}
			}
			TableItem item = new TableItem(swtTable, SWT.NONE);
			
			item.setText(data.toArray(new String[data.size()]));
			item.setData(record);
		}
	}
	
	
	/**
	 * {@link JmDataSet}編集テーブルにおける編集の終了を検知し、セルエディタの終了＆後処理を行うリスナ。
	 * 
	 * @author daisuke
	 */
	private final class FinishEditListener implements Listener {
		
		private final Text text;
		
		private final TableItem item;
		
		private final int columnIndex;
		
		
		/**
		 * インスタンスを生成する。
		 * 
		 * @param text セルエディタのコンポーネント
		 * @param item SWTテーブルアイテム
		 * @param columnIndex SWTテーブル内のカラムインデックス
		 */
		private FinishEditListener(Text text, TableItem item, int columnIndex) {
			this.text = text;
			this.item = item;
			this.columnIndex = columnIndex;
		}
		
		public void handleEvent(final Event e) {
			SimpleJmRecord record;
			JmColumn column;
			if (e.type == SWT.FocusOut) {
				logger.debug(LogMarker.LIFECYCLE, "focus out");
				item.setText(columnIndex, text.getText());
				record = (SimpleJmRecord) item.getData();
				column = (JmColumn) swtTable.getColumn(columnIndex).getData();
				Map<EntityRef<? extends JmColumn>, ScriptString> values = Maps.newHashMap(record.getValues());
				values.put(column.toReference(), new ScriptString(text.getText()));
				item.setData(new SimpleJmRecord(values));
				text.dispose();
			} else if (e.type == SWT.Traverse) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					logger.debug(LogMarker.LIFECYCLE, "traverse return");
					item.setText(columnIndex, text.getText());
					record = (SimpleJmRecord) item.getData();
					column = (JmColumn) swtTable.getColumn(columnIndex).getData();
					Map<EntityRef<? extends JmColumn>, ScriptString> values = Maps.newHashMap(record.getValues());
					values.put(column.toReference(), new ScriptString(text.getText()));
					item.setData(new SimpleJmRecord(values));
				}
				
				if (e.detail == SWT.TRAVERSE_RETURN || e.detail == SWT.TRAVERSE_ESCAPE) {
					logger.debug(LogMarker.LIFECYCLE, "traverse escape (or return fall through)");
					text.dispose();
					e.doit = false;
				}
			}
		}
	}
	
	/**
	 * {@link JmDataSet}編集テーブルにおける編集の開始を検知し、セルエディタの起動を行うリスナ。
	 * 
	 * @author daisuke
	 */
	private final class StartEditListener implements Listener {
		
		private final TableEditor editor;
		
		
		/**
		 * インスタンスを生成する。
		 * 
		 * @param editor テーブルエディタ
		 */
		private StartEditListener(TableEditor editor) {
			this.editor = editor;
		}
		
		public void handleEvent(Event event) {
			logger.debug(LogMarker.LIFECYCLE, "mouse down");
			Rectangle clientArea = swtTable.getClientArea();
			Point pt = new Point(event.x, event.y);
			int index = swtTable.getTopIndex();
			while (index < swtTable.getItemCount()) {
				boolean visible = false;
				final TableItem item = swtTable.getItem(index);
				for (int i = 0; i < swtTable.getColumnCount(); i++) {
					Rectangle rect = item.getBounds(i);
					if (rect.contains(pt)) {
						final int columnIndex = i;
						final Text text = new Text(swtTable, SWT.NONE);
						Listener textListener = new FinishEditListener(text, item, columnIndex);
						text.addListener(SWT.FocusOut, textListener);
						text.addListener(SWT.Traverse, textListener);
						editor.setEditor(text, item, i);
						text.setText(item.getText(i));
						text.selectAll();
						text.setFocus();
						return;
					}
					if (visible == false && rect.intersects(clientArea)) {
						visible = true;
					}
				}
				if (visible == false) {
					return;
				}
				index++;
			}
		}
	}
}
