/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
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
package org.jiemamy.eclipse.core.ui.editor.dialog.table;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
import org.jiemamy.eclipse.core.ui.editor.dialog.AbstractEditListener;
import org.jiemamy.eclipse.core.ui.editor.dialog.AbstractTab;
import org.jiemamy.eclipse.core.ui.editor.dialog.AbstractTableEditor;
import org.jiemamy.eclipse.core.ui.editor.dialog.DefaultTableEditorConfig;
import org.jiemamy.eclipse.core.ui.editor.dialog.EditListener;
import org.jiemamy.eclipse.core.ui.utils.KeyConstraintUtil;
import org.jiemamy.eclipse.core.ui.utils.TextSelectionAdapter;
import org.jiemamy.model.column.ColumnModel;
import org.jiemamy.model.constraint.AbstractConstraintModel;
import org.jiemamy.model.constraint.AbstractLocalKeyConstraintModel;
import org.jiemamy.model.constraint.CheckConstraintModel;
import org.jiemamy.model.constraint.ConstraintModel;
import org.jiemamy.model.constraint.DefaultCheckConstraintModel;
import org.jiemamy.model.constraint.DefaultNotNullConstraintModel;
import org.jiemamy.model.constraint.DefaultPrimaryKeyConstraintModel;
import org.jiemamy.model.constraint.DefaultUniqueKeyConstraintModel;
import org.jiemamy.model.constraint.ForeignKeyConstraintModel;
import org.jiemamy.model.constraint.KeyConstraintModel;
import org.jiemamy.model.constraint.LocalKeyConstraintModel;
import org.jiemamy.model.constraint.NotNullConstraintModel;
import org.jiemamy.model.constraint.PrimaryKeyConstraintModel;
import org.jiemamy.model.constraint.UniqueKeyConstraintModel;
import org.jiemamy.model.table.DefaultTableModel;
import org.jiemamy.model.table.TableModel;
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
	
	private final DefaultTableModel tableModel;
	
	private AbstractTableEditor constraintTableEditor;
	

	/**
	 * インスタンスを生成する。
	 * 
	 * @param parentTabFolder 親となるタブフォルダ
	 * @param style SWTスタイル値
	 * @param context コンテキスト
	 * @param tableModel 編集対象{@link TableModel}
	 */
	public TableEditDialogConstraintTab(TabFolder parentTabFolder, int style, JiemamyContext context,
			DefaultTableModel tableModel) {
		super(parentTabFolder, style, Messages.Tab_Table_Constraints);
		
		this.context = context;
		this.tableModel = tableModel;
		
		Composite composite = new Composite(parentTabFolder, SWT.NULL);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		constraintTableEditor = new ConstraintTableEditor(composite, SWT.NULL);
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
	private class ConstraintContentProvider implements IStructuredContentProvider, StoredEventListener {
		
		public void commandExecuted(StoredEvent<?> command) {
			logger.debug(LogMarker.LIFECYCLE, "commandExecuted");
			constraintTableEditor.refreshTable(); // レコードの変更を反映させる。
		}
		
		public void dispose() {
			logger.debug(LogMarker.LIFECYCLE, "disposed");
		}
		
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof TableModel) {
				Set<? extends ConstraintModel> constraints = ((TableModel) inputElement).getConstraints();
				List<ConstraintModel> constraintList = Lists.newArrayList(constraints);
				Collections.sort(constraintList, new ConstraintComparator());
				return constraintList.toArray();
			}
			logger.error("unknown input: " + inputElement.getClass().getName());
			return new Object[0];
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
	private class ConstraintLabelProvider extends BaseLabelProvider implements ITableLabelProvider {
		
		public Image getColumnImage(Object element, int columnIndex) {
			if ((element instanceof ConstraintModel) == false) {
				logger.error("unknown element: " + element.getClass().getName());
				return null;
			}
			
			if (element instanceof LocalKeyConstraintModel) {
				LocalKeyConstraintModel localKey = (LocalKeyConstraintModel) element;
				if (columnIndex == 0 && localKey instanceof PrimaryKeyConstraintModel) {
					ImageRegistry ir = JiemamyUIPlugin.getDefault().getImageRegistry();
					return ir.get(Images.ICON_PK);
				}
			}
			return null;
		}
		
		public String getColumnText(Object element, int columnIndex) {
			if ((element instanceof ConstraintModel) == false) {
				logger.error("unknown element: " + element.getClass().getName());
				return StringUtils.EMPTY;
			}
			ConstraintModel constraintModel = (ConstraintModel) element;
			switch (columnIndex) {
				case 0:
					return constraintToString(constraintModel);
					
				case 1:
					return constraintModel.getName();
					
				case 2:
					if (constraintModel instanceof KeyConstraintModel) {
						KeyConstraintModel keyConstraintModel = (KeyConstraintModel) constraintModel;
						return KeyConstraintUtil.toStringKeyColumns(context, keyConstraintModel);
					} else if (constraintModel instanceof CheckConstraintModel) {
						return ((CheckConstraintModel) constraintModel).getExpression();
					} else if (constraintModel instanceof NotNullConstraintModel) {
						EntityRef<? extends ColumnModel> ref =
								((NotNullConstraintModel) constraintModel).getColumnRef();
						if (ref != null) {
							try {
								ColumnModel targetColumn = tableModel.resolve(ref);
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
		private String constraintToString(ConstraintModel constraintModel) {
			if (constraintModel instanceof PrimaryKeyConstraintModel) {
				return "PK";
			} else if (constraintModel instanceof UniqueKeyConstraintModel) {
				return "UK";
			} else if (constraintModel instanceof ForeignKeyConstraintModel) {
				return "FK";
			} else if (constraintModel instanceof NotNullConstraintModel) {
				return "NN";
			} else if (constraintModel instanceof CheckConstraintModel) {
				return "CC";
			} else {
				return StringUtils.EMPTY;
			}
		}
	}
	
	private class ConstraintTableEditor extends AbstractTableEditor {
		
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
		public ConstraintTableEditor(Composite parent, int style) {
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
				public ConstraintModel getModel() {
					DefaultPrimaryKeyConstraintModel constraint =
							new DefaultPrimaryKeyConstraintModel(UUID.randomUUID());
					constraint.addKeyColumn(tableModel.getColumns().get(0).toReference());
					return constraint;
				}
			});
			
			MenuItem menuItemAddUk = new MenuItem(addMenu, SWT.PUSH);
			menuItemAddUk.setText("Unique Key Constraint"); // RESOURCE
			menuItemAddUk.addSelectionListener(new SelectionAdapterExtension() {
				
				@Override
				public ConstraintModel getModel() {
					DefaultUniqueKeyConstraintModel constraint = new DefaultUniqueKeyConstraintModel(UUID.randomUUID());
					constraint.addKeyColumn(tableModel.getColumns().get(0).toReference());
					return constraint;
				}
			});
			
			MenuItem menuItemAddFk = new MenuItem(addMenu, SWT.PUSH);
			menuItemAddFk.setText("Foreign Key Constraint"); // RESOURCE
			menuItemAddFk.setEnabled(false);
			// TODO
			
			MenuItem menuItemAddCc = new MenuItem(addMenu, SWT.PUSH);
			menuItemAddCc.setText("Check Constraint"); // RESOURCE
			menuItemAddCc.addSelectionListener(new SelectionAdapterExtension() {
				
				@Override
				public ConstraintModel getModel() {
					DefaultCheckConstraintModel constraint = new DefaultCheckConstraintModel(UUID.randomUUID());
					return constraint;
				}
			});
			
			MenuItem menuItemAddNn = new MenuItem(addMenu, SWT.PUSH);
			menuItemAddNn.setText("Not-null Constraint"); // RESOURCE
			menuItemAddNn.addSelectionListener(new SelectionAdapterExtension() {
				
				@Override
				public ConstraintModel getModel() {
					DefaultNotNullConstraintModel constraint = new DefaultNotNullConstraintModel(UUID.randomUUID());
					constraint.setColumn(tableModel.getColumns().get(0).toReference());
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
			tableViewer.setLabelProvider(new ConstraintLabelProvider());
			final ConstraintContentProvider contentProvider = new ConstraintContentProvider();
			tableViewer.setContentProvider(contentProvider);
			tableViewer.setInput(tableModel);
			
			final EventBroker eventBroker = tableModel.getEventBroker();
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
			ConstraintModel constraintModel = (ConstraintModel) getTableViewer().getElementAt(index);
			
			txtConstraintName.setEnabled(true);
			txtConstraintName.setText(StringUtils.EMPTY);
			txtCheckExpression.setEnabled(true);
			txtCheckExpression.setText(StringUtils.EMPTY);
			lstKeyColumns.setEnabled(true);
			lstKeyColumns.removeAll();
			lstTargetColumn.setEnabled(true);
			lstTargetColumn.removeAll();
			
			if (constraintModel instanceof CheckConstraintModel == false) {
				txtCheckExpression.setEnabled(false);
			}
			
			if (constraintModel instanceof KeyConstraintModel == false) {
				lstKeyColumns.setEnabled(false);
			}
			
			if (constraintModel instanceof NotNullConstraintModel == false) {
				lstTargetColumn.setEnabled(false);
			}
			
			// 現在値の設定
			txtConstraintName.setText(StringUtils.defaultString(constraintModel.getName()));
			if (constraintModel instanceof CheckConstraintModel) {
				CheckConstraintModel check = (CheckConstraintModel) constraintModel;
				txtCheckExpression.setText(StringUtils.defaultString(check.getExpression()));
			} else if (constraintModel instanceof KeyConstraintModel) {
				KeyConstraintModel key = (KeyConstraintModel) constraintModel;
				List<EntityRef<? extends ColumnModel>> keyColumns = key.getKeyColumns();
				List<ColumnModel> columns = tableModel.getColumns();
				for (ColumnModel columnModel : columns) {
					lstKeyColumns.add(columnModel.getName());
					boolean found = false;
					for (EntityRef<? extends ColumnModel> columnRef : keyColumns) {
						if (columnRef.isReferenceOf(columnModel)) {
							found = true;
							break;
						}
					}
					if (found) {
						int[] newIndices =
								ArrayUtils.add(lstKeyColumns.getSelectionIndices(), columns.indexOf(columnModel));
						lstKeyColumns.setSelection(newIndices);
					}
				}
			} else if (constraintModel instanceof NotNullConstraintModel) {
				NotNullConstraintModel nn = (NotNullConstraintModel) constraintModel;
				EntityRef<? extends ColumnModel> columnRef = nn.getColumnRef();
				List<ColumnModel> columns = tableModel.getColumns();
				for (int i = 0; i < columns.size(); i++) {
					ColumnModel columnModel = columns.get(i);
					lstTargetColumn.add(columnModel.getName());
					if (columnRef != null && columnRef.isReferenceOf(columnModel)) {
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
				
				if (tableModel.getPrimaryKey() != null || tableModel.getColumns().isEmpty()) {
					addMenu.getItem(0).setEnabled(false);
				} else {
					addMenu.getItem(0).setEnabled(true);
				}
				
				if (tableModel.getColumns().isEmpty()) {
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
			Table table = tableViewer.getTable();
			int index = table.getSelectionIndex();
			if (index < 0 || index > table.getItemCount()) {
				return;
			}
			
			ConstraintModel subject = (ConstraintModel) getTableViewer().getElementAt(index);
			tableModel.deleteConstraint(subject.toReference());
			
			tableViewer.remove(subject);
			int nextSelection = table.getItemCount() > index ? index : index - 1;
			if (nextSelection >= 0) {
				table.setSelection(nextSelection);
				enableEditorControls(nextSelection);
			} else {
				disableEditorControls();
			}
			table.setFocus();
		}
		
		private void updateModel() {
			int editIndex = getTableViewer().getTable().getSelectionIndex();
			
			if (editIndex == -1) {
				return;
			}
			
			AbstractConstraintModel constraintModel =
					(AbstractConstraintModel) getTableViewer().getElementAt(editIndex);
			
			String constraintName = StringUtils.defaultString(txtConstraintName.getText());
			constraintModel.setName(constraintName);
			
			if (constraintModel instanceof DefaultCheckConstraintModel) {
				DefaultCheckConstraintModel checkConstraintModel = (DefaultCheckConstraintModel) constraintModel;
				String expression = StringUtils.defaultString(txtCheckExpression.getText());
				checkConstraintModel.setExpression(expression);
			} else if (constraintModel instanceof AbstractLocalKeyConstraintModel) {
				AbstractLocalKeyConstraintModel localKeyConstraint = (AbstractLocalKeyConstraintModel) constraintModel;
				localKeyConstraint.clearKeyColumns();
				for (int selectionIndex : lstKeyColumns.getSelectionIndices()) {
					ColumnModel columnModel = tableModel.getColumns().get(selectionIndex);
					localKeyConstraint.addKeyColumn(columnModel.toReference());
				}
			} else if (constraintModel instanceof DefaultNotNullConstraintModel) {
				DefaultNotNullConstraintModel nn = (DefaultNotNullConstraintModel) constraintModel;
				int selectionIndex = lstTargetColumn.getSelectionIndex();
				if (selectionIndex >= 0) {
					ColumnModel columnModel = tableModel.getColumns().get(selectionIndex);
					nn.setColumn(columnModel.toReference());
				}
			}
			
			tableModel.store(constraintModel);
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
				Table table = getTableViewer().getTable();
				
				ConstraintModel constraintModel = getModel();
				tableModel.store(constraintModel);
				
				int addedIndex = Lists.transform(Arrays.asList(table.getItems()), new Function<TableItem, Object>() {
					
					public Object apply(TableItem item) {
						return item.getData();
					}
				}).indexOf(constraintModel);
				if (addedIndex < 0) {
					return;
				}
				table.setSelection(addedIndex);
				enableEditControls(addedIndex);
				txtConstraintName.setFocus();
			}
			
			abstract ConstraintModel getModel();
		}
	}
}
