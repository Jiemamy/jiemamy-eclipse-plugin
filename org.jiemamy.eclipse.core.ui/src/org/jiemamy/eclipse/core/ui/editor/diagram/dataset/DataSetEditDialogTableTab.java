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
import org.jiemamy.model.dataset.SimpleJmRecord;
import org.jiemamy.model.dataset.JmRecord;
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
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param parentTabFolder 親となるタブフォルダ
	 * @param style SWTスタイル値
	 * @param dataSetModel 編集対象{@link JmDataSet}
	 * @param tableModel {@link JmDataSet}内での対象テーブル
	 */
	public DataSetEditDialogTableTab(TabFolder parentTabFolder, int style, JmDataSet dataSetModel,
			JmTable tableModel) {
		super(parentTabFolder, style, tableModel.getName());
		getTabItem().setData(tableModel);
		
		Composite composite = new Composite(parentTabFolder, SWT.NULL);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		final Table table = new Table(composite, SWT.BORDER | SWT.MULTI);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setData(tableModel);
		
		List<JmColumn> columns = tableModel.getColumns();
		for (JmColumn columnModel : columns) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setWidth(COL_WIDTH);
			column.setText(columnModel.getName());
			column.setData(columnModel);
		}
		
		final List<JmRecord> records = dataSetModel.getRecords().get(tableModel.toReference());
		refreshTable(table, records);
		
		final TableEditor editor = new TableEditor(table);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		table.addListener(SWT.MouseDown, new StartEditListener(editor, table));
		
		getTabItem().setControl(composite);
		getTabItem().addListener(RECORD_CHANGED, new Listener() {
			
			public void handleEvent(Event event) {
				refreshTable(table, records);
			}
		});
	}
	
	@Override
	public boolean isTabComplete() {
		return true;
	}
	
	private void refreshTable(final Table table, List<JmRecord> records) {
		for (TableItem item : table.getItems()) {
			item.dispose();
		}
		
		for (JmRecord recordModel : records) {
			List<ScriptString> data = Lists.newArrayList();
			for (TableColumn tableColumn : table.getColumns()) {
				JmColumn columnModel = (JmColumn) tableColumn.getData();
				EntityRef<? extends JmColumn> columnRef = columnModel.toReference();
				ScriptString string = recordModel.getValues().get(columnRef);
				data.add(string);
			}
			TableItem item = new TableItem(table, SWT.NONE);
			
			List<String> strings = Lists.newArrayList();
			for (ScriptString scriptString : data) {
				strings.add(scriptString.getScript());
			}
			item.setText(strings.toArray(new String[data.size()]));
			item.setData(recordModel);
		}
	}
	

	/**
	 * {@link JmDataSet}編集テーブルにおける編集の開始を検知し、セルエディタの起動を行うリスナ。
	 * 
	 * @author daisuke
	 */
	private final class StartEditListener implements Listener {
		
		private final TableEditor editor;
		
		private final Table table;
		

		/**
		 * インスタンスを生成する。
		 * 
		 * @param editor テーブルエディタ
		 * @param table SWTテーブル
		 */
		private StartEditListener(TableEditor editor, Table table) {
			this.editor = editor;
			this.table = table;
		}
		
		public void handleEvent(Event event) {
			logger.debug(LogMarker.LIFECYCLE, "mouse down");
			Rectangle clientArea = table.getClientArea();
			Point pt = new Point(event.x, event.y);
			int index = table.getTopIndex();
			while (index < table.getItemCount()) {
				boolean visible = false;
				final TableItem item = table.getItem(index);
				for (int i = 0; i < table.getColumnCount(); i++) {
					Rectangle rect = item.getBounds(i);
					if (rect.contains(pt)) {
						final int columnIndex = i;
						final Text text = new Text(table, SWT.NONE);
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
				SimpleJmRecord recordModel;
				JmColumn columnModel;
				if (e.type == SWT.FocusOut) {
					logger.debug(LogMarker.LIFECYCLE, "focus out");
					item.setText(columnIndex, text.getText());
					recordModel = (SimpleJmRecord) item.getData();
					columnModel = (JmColumn) table.getColumn(columnIndex).getData();
					Map<EntityRef<? extends JmColumn>, ScriptString> values =
							Maps.newHashMap(recordModel.getValues());
					values.put(columnModel.toReference(), new ScriptString(text.getText()));
					item.setData(new SimpleJmRecord(values));
					text.dispose();
				} else if (e.type == SWT.Traverse) {
					if (e.detail == SWT.TRAVERSE_RETURN) {
						logger.debug(LogMarker.LIFECYCLE, "traverse return");
						item.setText(columnIndex, text.getText());
						recordModel = (SimpleJmRecord) item.getData();
						columnModel = (JmColumn) table.getColumn(columnIndex).getData();
						Map<EntityRef<? extends JmColumn>, ScriptString> values =
								Maps.newHashMap(recordModel.getValues());
						values.put(columnModel.toReference(), new ScriptString(text.getText()));
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
	}
}
