/*
 * Copyright 2007-2012 Jiemamy Project and the Others.
 * Created on 2009/02/18
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.JiemamyContext;
import org.jiemamy.dddbase.EntityNotFoundException;
import org.jiemamy.dddbase.EntityRef;
import org.jiemamy.eclipse.core.ui.Images;
import org.jiemamy.eclipse.core.ui.JiemamyUIPlugin;
import org.jiemamy.eclipse.core.ui.editor.diagram.AbstractEditListener;
import org.jiemamy.eclipse.core.ui.editor.diagram.AbstractTab;
import org.jiemamy.eclipse.core.ui.editor.diagram.AbstractTableEditor;
import org.jiemamy.eclipse.core.ui.editor.diagram.DefaultTableEditorConfig;
import org.jiemamy.eclipse.core.ui.editor.diagram.EditListener;
import org.jiemamy.eclipse.core.ui.utils.KeyConstraintUtil;
import org.jiemamy.eclipse.core.ui.utils.TextSelectionAdapter;
import org.jiemamy.model.column.JmColumn;
import org.jiemamy.model.constraint.JmCheckConstraint;
import org.jiemamy.model.constraint.JmConstraint;
import org.jiemamy.model.constraint.JmForeignKeyConstraint;
import org.jiemamy.model.constraint.JmKeyConstraint;
import org.jiemamy.model.constraint.JmLocalKeyConstraint;
import org.jiemamy.model.constraint.JmNotNullConstraint;
import org.jiemamy.model.constraint.JmPrimaryKeyConstraint;
import org.jiemamy.model.constraint.JmUniqueKeyConstraint;
import org.jiemamy.model.constraint.SimpleJmCheckConstraint;
import org.jiemamy.model.constraint.SimpleJmConstraint;
import org.jiemamy.model.constraint.SimpleJmLocalKeyConstraint;
import org.jiemamy.model.constraint.SimpleJmNotNullConstraint;
import org.jiemamy.model.constraint.SimpleJmPrimaryKeyConstraint;
import org.jiemamy.model.constraint.SimpleJmUniqueKeyConstraint;
import org.jiemamy.model.table.JmTable;
import org.jiemamy.model.table.SimpleJmTable;
import org.jiemamy.transaction.EventBroker;
import org.jiemamy.transaction.StoredEvent;
import org.jiemamy.transaction.StoredEventListener;
import org.jiemamy.utils.ConstraintComparator;
import org.jiemamy.utils.LogMarker;

/**
 * テーブル編集ダイアログの「制約」タブ。
 * 
 * @author daisuke
 */
public class TableEditDialogConstraintTab extends AbstractTab {
	
	private static Logger logger = LoggerFactory.getLogger(TableEditDialogConstraintTab.class);
	
	private final JiemamyContext context;
	
	private final SimpleJmTable table;
	
	private AbstractTableEditor constraintTableEditor;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param parentTabFolder 親となるタブフォルダ
	 * @param style SWTスタイル値
	 * @param context コンテキスト
	 * @param table 編集対象{@link JmTable}
	 */
	public TableEditDialogConstraintTab(TabFolder parentTabFolder, int style, JiemamyContext context,
			SimpleJmTable table) {
		super(parentTabFolder, style, Messages.Tab_Table_Constraints);
		
		this.context = context;
		this.table = table;
		
		Composite composite = new Composite(parentTabFolder, SWT.NULL);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		constraintTableEditor = new JmConstraintTableEditor(composite, SWT.NULL);
		constraintTableEditor.configure();
		constraintTableEditor.disableEditControls();
		
		getTabItem().setControl(composite);
	}
	
	@Override
	public boolean isTabComplete() {
		return true;
	}
	
	
	/**
	 * 制約用{@link IContentProvider}実装クラス。
	 * 
	 * @author daisuke
	 */
	private class JmConstraintContentProvider implements IStructuredContentProvider, StoredEventListener {
		
		public void dispose() {
			logger.debug(LogMarker.LIFECYCLE, "disposed");
		}
		
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof JmTable) {
				Set<? extends JmConstraint> constraints = ((JmTable) inputElement).getConstraints();
				List<JmConstraint> constraintList = Lists.newArrayList(constraints);
				Collections.sort(constraintList, ConstraintComparator.INSTANCE);
				return constraintList.toArray();
			}
			logger.error("unknown input: " + inputElement.getClass().getName());
			return new Object[0];
		}
		
		public void handleStoredEvent(StoredEvent<?> event) {
			logger.debug(LogMarker.LIFECYCLE, "commandExecuted");
			constraintTableEditor.refreshTable(); // レコードの変更を反映させる。
		}
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			logger.debug(LogMarker.LIFECYCLE, "input changed");
			logger.trace(LogMarker.LIFECYCLE, "oldInput: " + oldInput);
			logger.trace(LogMarker.LIFECYCLE, "newInput: " + newInput);
		}
	}
	
	/**
	 * 制約用{@link ITableLabelProvider}実装クラス。
	 * 
	 * @author daisuke
	 */
	private class JmConstraintLabelProvider extends BaseLabelProvider implements ITableLabelProvider {
		
		public Image getColumnImage(Object element, int columnIndex) {
			if ((element instanceof JmConstraint) == false) {
				logger.error("unknown element: " + element.getClass().getName());
				return null;
			}
			
			if (element instanceof JmLocalKeyConstraint) {
				JmLocalKeyConstraint localKey = (JmLocalKeyConstraint) element;
				if (columnIndex == 0 && localKey instanceof JmPrimaryKeyConstraint) {
					ImageRegistry ir = JiemamyUIPlugin.getDefault().getImageRegistry();
					return ir.get(Images.ICON_PK);
				}
			}
			return null;
		}
		
		public String getColumnText(Object element, int columnIndex) {
			if ((element instanceof JmConstraint) == false) {
				logger.error("unknown element: " + element.getClass().getName());
				return StringUtils.EMPTY;
			}
			JmConstraint constraint = (JmConstraint) element;
			switch (columnIndex) {
				case 0:
					return constraintToString(constraint);
					
				case 1:
					return constraint.getName();
					
				case 2:
					if (constraint instanceof JmKeyConstraint) {
						JmKeyConstraint keyConstraint = (JmKeyConstraint) constraint;
						return KeyConstraintUtil.toStringKeyColumns(context, keyConstraint);
					} else if (constraint instanceof JmCheckConstraint) {
						return ((JmCheckConstraint) constraint).getExpression();
					} else if (constraint instanceof JmNotNullConstraint) {
						EntityRef<? extends JmColumn> ref = ((JmNotNullConstraint) constraint).getColumn();
						if (ref != null) {
							try {
								JmColumn targetColumn = table.resolve(ref);
								return targetColumn.getName();
							} catch (EntityNotFoundException e) {
								// ignore
							}
						}
					}
					return StringUtils.EMPTY;
					
				default:
					return StringUtils.EMPTY;
			}
		}
		
		// TODO utilじゃね？
		private String constraintToString(JmConstraint constraint) {
			if (constraint instanceof JmPrimaryKeyConstraint) {
				return "PK";
			} else if (constraint instanceof JmUniqueKeyConstraint) {
				return "UK";
			} else if (constraint instanceof JmForeignKeyConstraint) {
				return "FK";
			} else if (constraint instanceof JmNotNullConstraint) {
				return "NN";
			} else if (constraint instanceof JmCheckConstraint) {
				return "CC";
			} else {
				return StringUtils.EMPTY;
			}
		}
	}
	
	private class JmConstraintTableEditor extends AbstractTableEditor {
		
		private static final int COL_WIDTH_CATEGORY = 50;
		
		private static final int COL_WIDTH_NAME = 160;
		
		private static final int COL_WIDTH_DATA = 300;
		
		private final EditListener editListener = new EditListenerImpl();
		
		private Menu addMenu;
		
		private Text txtConstraintName;
		
		private Text txtCheckExpression;
		
		private org.eclipse.swt.widgets.List lstKeyColumns;
		
		private org.eclipse.swt.widgets.List lstTargetColumn;
		
		
		/**
		 * インスタンスを生成する。
		 * 
		 * @param parent 親コンポーネント
		 * @param style SWTスタイル値
		 */
		public JmConstraintTableEditor(Composite parent, int style) {
			super(parent, style, new DefaultTableEditorConfig("制約情報") { // RESOURCE
					
						@Override
						public String getInsertLabel() {
							return null;
						}
						
						@Override
						public boolean isFreeOrder() {
							return false;
						}
					});
		}
		
		@Override
		protected void configureEditButtons() {
			super.configureEditButtons();
			
			addMenu = new Menu(getBtnAdd());
			
			MenuItem menuItemAddPk = new MenuItem(addMenu, SWT.PUSH);
			menuItemAddPk.setText("Primary Key Constraint"); // RESOURCE
			menuItemAddPk.addSelectionListener(new SelectionAdapterExtension() {
				
				@Override
				public JmConstraint getModel() {
					SimpleJmPrimaryKeyConstraint constraint = new SimpleJmPrimaryKeyConstraint();
					constraint.addKeyColumn(table.getColumns().get(0).toReference());
					return constraint;
				}
			});
			
			MenuItem menuItemAddUk = new MenuItem(addMenu, SWT.PUSH);
			menuItemAddUk.setText("Unique Key Constraint"); // RESOURCE
			menuItemAddUk.addSelectionListener(new SelectionAdapterExtension() {
				
				@Override
				public JmConstraint getModel() {
					SimpleJmUniqueKeyConstraint constraint = new SimpleJmUniqueKeyConstraint();
					constraint.addKeyColumn(table.getColumns().get(0).toReference());
					return constraint;
				}
			});
			
			MenuItem menuItemAddFk = new MenuItem(addMenu, SWT.PUSH);
			menuItemAddFk.setText("Foreign Key Constraint"); // RESOURCE
			menuItemAddFk.setEnabled(false);
			// 今は常に無効だが、いつかmenuからFKを追加できるようにするといいかも。
			
			MenuItem menuItemAddCc = new MenuItem(addMenu, SWT.PUSH);
			menuItemAddCc.setText("Check Constraint"); // RESOURCE
			menuItemAddCc.addSelectionListener(new SelectionAdapterExtension() {
				
				@Override
				public JmConstraint getModel() {
					SimpleJmCheckConstraint constraint = new SimpleJmCheckConstraint();
					return constraint;
				}
			});
			
			MenuItem menuItemAddNn = new MenuItem(addMenu, SWT.PUSH);
			menuItemAddNn.setText("Not-null Constraint"); // RESOURCE
			menuItemAddNn.addSelectionListener(new SelectionAdapterExtension() {
				
				@Override
				public JmConstraint getModel() {
					SimpleJmNotNullConstraint constraint = new SimpleJmNotNullConstraint();
					constraint.setColumn(table.getColumns().get(0).toReference());
					return constraint;
				}
			});
		}
		
		@Override
		protected void configureEditorControls() {
			super.configureEditorControls();
			
			txtConstraintName.addFocusListener(new TextSelectionAdapter(txtConstraintName));
			txtConstraintName.addKeyListener(editListener);
			
			txtCheckExpression.addFocusListener(new TextSelectionAdapter(txtCheckExpression));
			txtCheckExpression.addKeyListener(editListener);
			
			lstKeyColumns.addSelectionListener(editListener);
			
			lstTargetColumn.addSelectionListener(editListener);
		}
		
		@Override
		protected void configureTableViewer(TableViewer tableViewer) {
			tableViewer.setLabelProvider(new JmConstraintLabelProvider());
			final JmConstraintContentProvider contentProvider = new JmConstraintContentProvider();
			tableViewer.setContentProvider(contentProvider);
			tableViewer.setInput(table);
			
			final EventBroker eventBroker = table.getEventBroker();
			eventBroker.addListener(contentProvider);
			
			// THINK んーーー？？ このタイミングか？ AbstractTableEditor#dispose かな？
			tableViewer.getTable().addDisposeListener(new DisposeListener() {
				
				public void widgetDisposed(DisposeEvent e) {
					eventBroker.removeListener(contentProvider);
				}
				
			});
		}
		
		@Override
		protected void createEditorControls(Composite parent) {
			Label label;
			
			Composite cmpNames = new Composite(parent, SWT.NULL);
			cmpNames.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			GridLayout layout = new GridLayout(4, false);
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			cmpNames.setLayout(layout);
			
			label = new Label(cmpNames, SWT.NULL);
			label.setText("制約名(&M)"); // RESOURCE
			
			txtConstraintName = new Text(cmpNames, SWT.BORDER);
			txtConstraintName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			label = new Label(cmpNames, SWT.NULL);
			label.setText("チェック制約式(&P)"); // RESOURCE
			
			txtCheckExpression = new Text(cmpNames, SWT.BORDER);
			txtCheckExpression.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			label = new Label(cmpNames, SWT.NULL);
			label.setText("キー構成カラム(&O)"); // RESOURCE
			
			lstKeyColumns = new org.eclipse.swt.widgets.List(cmpNames, SWT.BORDER | SWT.MULTI);
			lstKeyColumns.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			label = new Label(cmpNames, SWT.NULL);
			label.setText("Not-null対象カラム(&R)"); // RESOURCE
			
			lstTargetColumn = new org.eclipse.swt.widgets.List(cmpNames, SWT.BORDER | SWT.SINGLE);
			lstTargetColumn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		
		@Override
		protected void createTableColumns(Table table) {
			TableColumn colCategory = new TableColumn(table, SWT.LEFT);
			colCategory.setText("種別"); // RESOURCE
			colCategory.setWidth(COL_WIDTH_CATEGORY);
			
			TableColumn colName = new TableColumn(table, SWT.LEFT);
			colName.setText("制約名"); // RESOURCE
			colName.setWidth(COL_WIDTH_NAME);
			
			TableColumn colData = new TableColumn(table, SWT.LEFT);
			colData.setText("キー構成カラム/NN対象カラム/チェック制約式"); // RESOURCE
			colData.setWidth(COL_WIDTH_DATA);
		}
		
		@Override
		protected void disableEditorControls() {
			txtConstraintName.setEnabled(false);
			txtCheckExpression.setEnabled(false);
			lstKeyColumns.setEnabled(false);
			lstTargetColumn.setEnabled(false);
			
			txtConstraintName.setText(StringUtils.EMPTY);
			txtCheckExpression.setText(StringUtils.EMPTY);
			lstKeyColumns.removeAll();
			lstTargetColumn.removeAll();
		}
		
		@Override
		protected void enableEditorControls(int index) {
			JmConstraint constraint = (JmConstraint) getTableViewer().getElementAt(index);
			
			txtConstraintName.setEnabled(true);
			txtConstraintName.setText(StringUtils.EMPTY);
			txtCheckExpression.setEnabled(true);
			txtCheckExpression.setText(StringUtils.EMPTY);
			lstKeyColumns.setEnabled(true);
			lstKeyColumns.removeAll();
			lstTargetColumn.setEnabled(true);
			lstTargetColumn.removeAll();
			
			if (constraint instanceof JmCheckConstraint == false) {
				txtCheckExpression.setEnabled(false);
			}
			
			if (constraint instanceof JmKeyConstraint == false) {
				lstKeyColumns.setEnabled(false);
			}
			
			if (constraint instanceof JmNotNullConstraint == false) {
				lstTargetColumn.setEnabled(false);
			}
			
			// 現在値の設定
			txtConstraintName.setText(StringUtils.defaultString(constraint.getName()));
			if (constraint instanceof JmCheckConstraint) {
				JmCheckConstraint check = (JmCheckConstraint) constraint;
				txtCheckExpression.setText(StringUtils.defaultString(check.getExpression()));
			} else if (constraint instanceof JmKeyConstraint) {
				JmKeyConstraint key = (JmKeyConstraint) constraint;
				List<EntityRef<? extends JmColumn>> keyColumns = key.getKeyColumns();
				List<JmColumn> columns = table.getColumns();
				for (JmColumn column : columns) {
					lstKeyColumns.add(column.getName());
					boolean found = false;
					for (EntityRef<? extends JmColumn> columnRef : keyColumns) {
						if (columnRef.isReferenceOf(column)) {
							found = true;
							break;
						}
					}
					if (found) {
						int[] newIndices = ArrayUtils.add(lstKeyColumns.getSelectionIndices(), columns.indexOf(column));
						lstKeyColumns.setSelection(newIndices);
					}
				}
			} else if (constraint instanceof JmNotNullConstraint) {
				JmNotNullConstraint nn = (JmNotNullConstraint) constraint;
				EntityRef<? extends JmColumn> columnRef = nn.getColumn();
				List<JmColumn> columns = table.getColumns();
				for (int i = 0; i < columns.size(); i++) {
					JmColumn column = columns.get(i);
					lstTargetColumn.add(column.getName());
					if (columnRef != null && columnRef.isReferenceOf(column)) {
						lstTargetColumn.setSelection(i);
					}
				}
			}
		}
		
		@Override
		protected void performAddItem() {
			if (addMenu.isVisible() == false) {
				Button button = getBtnAdd();
				Rectangle bounds = button.getBounds();
				Point menuLoc = button.getParent().toDisplay(bounds.x, bounds.y + bounds.height);
				
				if (table.getPrimaryKey() != null || table.getColumns().isEmpty()) {
					addMenu.getItem(0).setEnabled(false);
				} else {
					addMenu.getItem(0).setEnabled(true);
				}
				
				if (table.getColumns().isEmpty()) {
					addMenu.getItem(1).setEnabled(false); // UK
					addMenu.getItem(2).setEnabled(false); // FK
					addMenu.getItem(3).setEnabled(true); // CC
					addMenu.getItem(4).setEnabled(false); // NN
				} else {
					addMenu.getItem(1).setEnabled(true); // UK
					addMenu.getItem(2).setEnabled(false); // FK
					addMenu.getItem(3).setEnabled(true); // CC
					addMenu.getItem(4).setEnabled(true); // NN
				}
				
				addMenu.setLocation(menuLoc.x, menuLoc.y);
				addMenu.setVisible(true);
			}
		}
		
		@Override
		protected void performRemoveItem() {
			TableViewer tableViewer = getTableViewer();
			Table swtTable = tableViewer.getTable();
			int index = swtTable.getSelectionIndex();
			if (index < 0 || index > swtTable.getItemCount()) {
				return;
			}
			
			JmConstraint subject = (JmConstraint) getTableViewer().getElementAt(index);
			table.deleteConstraint(subject.toReference());
			
			tableViewer.remove(subject);
			int nextSelection = swtTable.getItemCount() > index ? index : index - 1;
			if (nextSelection >= 0) {
				swtTable.setSelection(nextSelection);
				enableEditorControls(nextSelection);
			} else {
				disableEditorControls();
			}
			swtTable.setFocus();
		}
		
		private void updateModel() {
			int editIndex = getTableViewer().getTable().getSelectionIndex();
			
			if (editIndex == -1) {
				return;
			}
			
			SimpleJmConstraint constraint = (SimpleJmConstraint) getTableViewer().getElementAt(editIndex);
			
			String constraintName = StringUtils.defaultString(txtConstraintName.getText());
			constraint.setName(constraintName);
			
			if (constraint instanceof SimpleJmCheckConstraint) {
				SimpleJmCheckConstraint checkConstraint = (SimpleJmCheckConstraint) constraint;
				String expression = StringUtils.defaultString(txtCheckExpression.getText());
				checkConstraint.setExpression(expression);
			} else if (constraint instanceof SimpleJmLocalKeyConstraint) {
				SimpleJmLocalKeyConstraint localJmKeyConstraint = (SimpleJmLocalKeyConstraint) constraint;
				localJmKeyConstraint.clearKeyColumns();
				for (int selectionIndex : lstKeyColumns.getSelectionIndices()) {
					JmColumn column = table.getColumns().get(selectionIndex);
					localJmKeyConstraint.addKeyColumn(column.toReference());
				}
			} else if (constraint instanceof SimpleJmNotNullConstraint) {
				SimpleJmNotNullConstraint nn = (SimpleJmNotNullConstraint) constraint;
				int selectionIndex = lstTargetColumn.getSelectionIndex();
				if (selectionIndex >= 0) {
					JmColumn column = table.getColumns().get(selectionIndex);
					nn.setColumn(column.toReference());
				}
			}
			
			table.store(constraint);
		}
		
		
		private class EditListenerImpl extends AbstractEditListener {
			
			@Override
			protected void process(TypedEvent e) {
				updateModel();
			}
		}
		
		private abstract class SelectionAdapterExtension extends SelectionAdapter {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Table swtTable = getTableViewer().getTable();
				
				JmConstraint constraint = getModel();
				table.store(constraint);
				
				int addedIndex = Lists.transform(Arrays.asList(swtTable.getItems()), new Function<TableItem, Object>() {
					
					public Object apply(TableItem item) {
						return item.getData();
					}
				}).indexOf(constraint);
				if (addedIndex < 0) {
					return;
				}
				swtTable.setSelection(addedIndex);
				enableEditControls(addedIndex);
				txtConstraintName.setFocus();
			}
			
			abstract JmConstraint getModel();
		}
	}
}
