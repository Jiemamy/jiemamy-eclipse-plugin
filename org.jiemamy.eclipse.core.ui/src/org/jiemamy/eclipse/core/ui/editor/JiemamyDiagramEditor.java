/*
 * Copyright 2007-2011 Jiemamy Project and the Others.
 * Created on 2008/07/29
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
package org.jiemamy.eclipse.core.ui.editor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.EventObject;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.AlignmentAction;
import org.eclipse.gef.ui.actions.DirectEditAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.MatchHeightAction;
import org.eclipse.gef.ui.actions.MatchWidthAction;
import org.eclipse.gef.ui.actions.SelectAllAction;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jiemamy.JmMetadata;
import org.jiemamy.SimpleJmMetadata;
import org.jiemamy.DiagramFacet;
import org.jiemamy.JiemamyContext;
import org.jiemamy.SqlFacet;
import org.jiemamy.dialect.Dialect;
import org.jiemamy.dialect.GenericDialect;
import org.jiemamy.eclipse.core.ui.editor.diagram.DiagramEditPartFactory;
import org.jiemamy.eclipse.core.ui.utils.ExceptionHandler;
import org.jiemamy.eclipse.core.ui.utils.MarkerUtil;
import org.jiemamy.model.SimpleJmDiagram;
import org.jiemamy.serializer.JiemamySerializer;
import org.jiemamy.serializer.SerializationException;
import org.jiemamy.transaction.EventBrokerImpl;
import org.jiemamy.transaction.StoredEvent;
import org.jiemamy.transaction.StoredEventListener;
import org.jiemamy.utils.LogMarker;
import org.jiemamy.utils.UUIDUtil;
import org.jiemamy.validator.AllValidator;
import org.jiemamy.validator.Problem;
import org.jiemamy.validator.Problem.Severity;
import org.jiemamy.validator.Validator;

/**
 * ERダイアグラムエディタ本体の実装クラス。
 * 
 * @author daisuke
 */
public class JiemamyDiagramEditor extends GraphicalEditorWithFlyoutPalette implements IResourceChangeListener,
		StoredEventListener, JiemamyEditor {
	
	private static Logger logger = LoggerFactory.getLogger(JiemamyDiagramEditor.class);
	
	/** Palette component, holding the tools and shapes. */
	private static PaletteRoot palette;
	
	/** DELキーのキーコード */
	private static final int KEYCODE_DEL = 127;
	
	/** zoom level */
	private static final double[] ZOOM_LEVELS = new double[] {
		0.1,
		0.3,
		0.4,
		0.5,
		0.6,
		0.7,
		0.8,
		0.9,
		1.0,
		1.2,
		1.5,
		2.0,
		2.5,
		3.0,
		5.0,
		7.0,
		10.0
	};
	

	private static int findSeverity(Severity severity) {
		if (severity == Severity.ERROR || severity == Severity.FATAL) {
			return IMarker.SEVERITY_ERROR;
		} else if (severity == Severity.WARN) {
			return IMarker.SEVERITY_WARNING;
		} else if (severity == Severity.INFO || severity == Severity.NOTICE) {
			return IMarker.SEVERITY_INFO;
		}
		return -1;
	}
	

	/** ルートEditPart（コントローラ） */
	private ScalableRootEditPart rootEditPart = new ScalableRootEditPart();
	
	/** このエディタに対応する{@link JiemamyContext}（モデル） */
	private JiemamyContext context;
	
	private boolean savePreviouslyNeeded = false;
	
	/** このエディタのタブインデックス */
	private int tabIndex;
	

	/**
	 * インスタンスを生成する。
	 */
	public JiemamyDiagramEditor() {
		setEditDomain(new DefaultEditDomain(this));
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		logger.debug(LogMarker.LIFECYCLE, "constructed - single");
	}
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param context {@link JiemamyContext}
	 * @param tabIndex マルチタブエディタ上でのタブインデックス
	 */
	public JiemamyDiagramEditor(JiemamyContext context, int tabIndex) {
		setEditDomain(new DefaultEditDomain(this));
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		this.tabIndex = tabIndex;
		logger.debug(LogMarker.LIFECYCLE, "constructed - multi");
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>この実装では、モデルの変更を検知して、{@link IMarker} (problem marker) の更新を行う。</p>
	 */
	public void commandExecuted(StoredEvent<?> command) {
		Validator validator;
		try {
			Dialect dialect = context.findDialect();
			validator = dialect.getValidator();
		} catch (IllegalStateException e) {
			configureSimpleDialect();
			validator = new AllValidator();
		} catch (ClassNotFoundException e) {
			configureSimpleDialect();
			validator = new AllValidator();
		}
		IResource resource = (IResource) getEditorInput().getAdapter(IResource.class);
		MarkerUtil.deleteAllMarkers();
		for (Problem problem : validator.validate(context)) {
			Severity severity = problem.getSeverity();
			String message =
					MessageFormat.format("{0}:{1} - {2}", problem.getErrorCode(), problem.getMessage(),
							UUIDUtil.toShortString(problem.getTargetId()));
			MarkerUtil.createMarker(resource, IMarker.PRIORITY_NORMAL, findSeverity(severity), message);
		}
	}
	
	@Override
	public void commandStackChanged(EventObject event) {
		if (isDirty()) {
			if (savePreviouslyNeeded == false) {
				savePreviouslyNeeded = true;
				firePropertyChange(IEditorPart.PROP_DIRTY);
			}
		} else {
			savePreviouslyNeeded = false;
			firePropertyChange(IEditorPart.PROP_DIRTY);
		}
		super.commandStackChanged(event);
	}
	
	@Override
	public void dispose() {
		context.getEventBroker().removeListener(this);
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
		logger.debug(LogMarker.LIFECYCLE, "disposed");
		
		// 以下debugコード
		if (JiemamyContext.isDebug()) {
			List<StoredEventListener> listeners = ((EventBrokerImpl) context.getEventBroker()).getListeners();
			for (StoredEventListener listener : listeners) {
				logger.warn(listener + " is not removed from EventBroker.");
			}
		}
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		ByteArrayOutputStream out = null;
		ByteArrayInputStream in = null;
		try {
			out = new ByteArrayOutputStream();
			JiemamyContext.findSerializer().serialize(context, out);
			
			in = new ByteArrayInputStream(out.toByteArray());
			IFile file = ((IFileEditorInput) getEditorInput()).getFile();
			file.setContents(in, true, true, monitor);
			getCommandStack().markSaveLocation();
		} catch (Exception e) {
			ExceptionHandler.handleException(e);
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
	}
	
	@Override
	public void doSaveAs() {
		Shell shell = getSite().getWorkbenchWindow().getShell();
		SaveAsDialog dialog = new SaveAsDialog(shell);
		dialog.setOriginalFile(((IFileEditorInput) getEditorInput()).getFile());
		dialog.open();
		
		IPath path = dialog.getResult();
		if (path == null) {
			return;
		}
		
		// try to save the editor's contents under a different file name
		final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		try {
			new ProgressMonitorDialog(shell).run(false, // don't fork
					false, // not cancelable
					new WorkspaceModifyOperation() { // run this operation
					
						@Override
						public void execute(IProgressMonitor monitor) {
							ByteArrayOutputStream out = null;
							ByteArrayInputStream in = null;
							try {
								out = new ByteArrayOutputStream();
								JiemamyContext.findSerializer().serialize(context, out);
								
								in = new ByteArrayInputStream(out.toByteArray());
								file.create(in, true, monitor);
							} catch (Exception e) {
								ExceptionHandler.handleException(e);
							} finally {
								IOUtils.closeQuietly(in);
								IOUtils.closeQuietly(out);
							}
						}
					});
			setInput(new FileEditorInput(file));
			getCommandStack().markSaveLocation();
		} catch (InterruptedException e) {
			// should not happen, since the monitor dialog is not cancelable
			ExceptionHandler.handleException(e);
		} catch (InvocationTargetException e) {
			ExceptionHandler.handleException(e);
		}
	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		// ↑Java1.4対応APIのため、Classに型パラメータをつけることができない
		if (adapter == ZoomManager.class) {
			return ((ScalableRootEditPart) getGraphicalViewer().getRootEditPart()).getZoomManager();
//		} else if (adapter == IContentOutlinePage.class) {
//			return new DiagramOutlinePage(new org.eclipse.gef.ui.parts.TreeViewer()); // GEFツリービューワを使用
		}
		return super.getAdapter(adapter);
	}
	
	/**
	 * {@link JiemamyContext}を取得する。
	 * 
	 * @return エディタのルートモデル
	 */
	public JiemamyContext getJiemamyContext() {
		return context;
	}
	
	/**
	 * このエディタのタブインデックスを取得する。
	 * 
	 * @return タブインデックス
	 */
	public int getTabIndex() {
		return tabIndex;
	}
	
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		
//		context = new JiemamyContext(DiagramFacet.PROVIDER, SqlFacet.PROVIDER);
//		
//		// FIXME 無差別ディスパッチになってる。
//		context.getEventBroker().setDefaultStrategy(new DispatchStrategy() {
//			
//			public boolean needToDispatch(StoredEventListener listener, StoredEvent<?> command) {
//				return true;
//			}
//			
//		});
//		context.getEventBroker().addListener(this);
		
		logger.debug(LogMarker.LIFECYCLE, "initialized");
	}
	
	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * エディタ外などからの、リソースの変更を検知する。
	 */
	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
			final IEditorInput input = getEditorInput();
			if (input instanceof IFileEditorInput) {
				Display.getDefault().asyncExec(new Runnable() {
					
					public void run() {
						IFile file = ((IFileEditorInput) input).getFile();
						if (file.exists() == false) {
							IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
							page.closeEditor(JiemamyDiagramEditor.this, true);
						} else if (getPartName().equals(file.getName()) == false) {
							setPartName(file.getName());
						}
					}
				});
			}
		}
	}
	
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part.getSite().getWorkbenchWindow().getActivePage() == null) {
			return;
		}
		super.selectionChanged(part, selection);
	}
	
	@Override
	public void setFocus() {
		super.setFocus();
		
		JmActionBarContributor contributor = (JmActionBarContributor) getEditorSite().getActionBarContributor();
		if (contributor != null) {
			contributor.selectCombo(context);
		}
		// Thanks to Naokiさん
		logger.debug(LogMarker.LIFECYCLE, "setFocus");
	}
	
	/**
	 * このエディタのタブインデックスを設定する。
	 * 
	 * @param tabIndex タブインデックス
	 */
	public void setTabIndex(int tabIndex) {
		this.tabIndex = tabIndex;
	}
	
	/**
	 * Describes this EditPart for developmental debugging
	 * purposes.
	 * @return a description
	 */
	@Override
	public String toString() {
		String c = getClass().getName();
		c = c.substring(c.lastIndexOf('.') + 1);
		return c + "( " + getEditorInput() + " )"; // $NON-NLS-2$ // $NON-NLS-1$
	}
	
	@Override
	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();
		
		// EditPartFactoryの作成と設定
		GraphicalViewer viewer = getGraphicalViewer();
		viewer.setEditPartFactory(new DiagramEditPartFactory(this));
		viewer.setRootEditPart(rootEditPart);
		
		ActionRegistry actionRegistry = getActionRegistry();
		
		// to make 'del' and 'f2' key work
		GraphicalViewerKeyHandler keyHandler = new GraphicalViewerKeyHandler(viewer);
		keyHandler.put(KeyStroke.getPressed(SWT.DEL, KEYCODE_DEL, 0),
				actionRegistry.getAction(ActionFactory.DELETE.getId()));
		keyHandler.put(KeyStroke.getPressed(SWT.F2, 0), actionRegistry.getAction(GEFActionConstants.DIRECT_EDIT));
		viewer.setKeyHandler(keyHandler);
		
		// configure the context menu provider
		viewer.setContextMenu(new DiagramEditorContextMenuProvider(viewer, this, actionRegistry));
		getSite().setSelectionProvider(viewer);
		
		logger.debug(LogMarker.LIFECYCLE, "GraphicalViewer configured");
	}
	
	@Override
	protected void createActions() {
		super.createActions();
		
		IAction action;
		ActionRegistry actionRegistry = getActionRegistry();
		IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
		
		// ZoomManager
		ZoomManager zoomManager = rootEditPart.getZoomManager();
		
		// zoom contribution
		List<String> zoomContributions = Lists.newArrayListWithCapacity(3);
		zoomContributions.add(ZoomManager.FIT_ALL);
		zoomContributions.add(ZoomManager.FIT_HEIGHT);
		zoomContributions.add(ZoomManager.FIT_WIDTH);
		zoomManager.setZoomLevelContributions(zoomContributions);
		
		zoomManager.setZoomLevels(ZOOM_LEVELS);
		
		@SuppressWarnings("unchecked")
		// このメソッドはString型のリストを返すことが保証されている
		List<String> selectionActions = getSelectionActions();
		
		// zoom level contribution
		action = new ZoomInAction(zoomManager);
		actionRegistry.registerAction(action);
		handlerService.activateHandler(action.getActionDefinitionId(), new ActionHandler(action));
		selectionActions.add(action.getId());
		
		action = new ZoomOutAction(zoomManager);
		actionRegistry.registerAction(action);
		handlerService.activateHandler(action.getActionDefinitionId(), new ActionHandler(action));
		selectionActions.add(action.getId());
		
		// select action
		action = new SelectAllAction(this);
		actionRegistry.registerAction(action);
		
		// match size contribution
		action = new MatchWidthAction(this);
		actionRegistry.registerAction(action);
		selectionActions.add(action.getId());
		
		action = new MatchHeightAction(this);
		actionRegistry.registerAction(action);
		selectionActions.add(action.getId());
		
		// direct edit contribution
		action = new DirectEditAction((IWorkbenchPart) this);
		actionRegistry.registerAction(action);
		// 選択オブジェクトによってアクションを更新する必要がある場合には
		// 以下のようにして、そのアクションのIDを登録しておく
		selectionActions.add(action.getId());
		
		// alignment contribution
		action = new AlignmentAction((IWorkbenchPart) this, PositionConstants.LEFT);
		actionRegistry.registerAction(action);
		selectionActions.add(action.getId());
		
		action = new AlignmentAction((IWorkbenchPart) this, PositionConstants.RIGHT);
		actionRegistry.registerAction(action);
		selectionActions.add(action.getId());
		
		action = new AlignmentAction((IWorkbenchPart) this, PositionConstants.TOP);
		actionRegistry.registerAction(action);
		selectionActions.add(action.getId());
		
		action = new AlignmentAction((IWorkbenchPart) this, PositionConstants.BOTTOM);
		actionRegistry.registerAction(action);
		selectionActions.add(action.getId());
		
		action = new AlignmentAction((IWorkbenchPart) this, PositionConstants.CENTER);
		actionRegistry.registerAction(action);
		selectionActions.add(action.getId());
		
		action = new AlignmentAction((IWorkbenchPart) this, PositionConstants.MIDDLE);
		actionRegistry.registerAction(action);
		selectionActions.add(action.getId());
		
		logger.debug(LogMarker.LIFECYCLE, "action created");
	}
	
	@Override
	protected PaletteRoot getPaletteRoot() {
		if (palette == null) {
			palette = DiagramEditorPaletteFactory.createPalette();
			logger.debug(LogMarker.LIFECYCLE, "palette created");
		}
		
		return palette;
	}
	
	@Override
	protected void initializeGraphicalViewer() {
		super.initializeGraphicalViewer();
		
		GraphicalViewer viewer = getGraphicalViewer();
		
		// 最上位モデルの設定
		IFile file = ((IFileEditorInput) getEditorInput()).getFile();
		try {
			JiemamySerializer serializer = JiemamyContext.findSerializer();
			context = serializer.deserialize(file.getContents(), DiagramFacet.PROVIDER, SqlFacet.PROVIDER);
			context.getEventBroker().addListener(this); // THINK require?
		} catch (SerializationException e) {
			ExceptionHandler.handleException(e, "Data file is broken.");
		} catch (CoreException e) {
			ExceptionHandler.handleException(e, ExceptionHandler.DIALOG,
					"May be, resource is not synchronized.  Try to hit F5 to refresh workspace.");
		} catch (Exception e) {
			ExceptionHandler.handleException(e);
		} finally {
			DiagramFacet diagramPresentations = context.getFacet(DiagramFacet.class);
			if (diagramPresentations.getDiagrams().size() < 1) {
				SimpleJmDiagram diagram = new SimpleJmDiagram(UUID.randomUUID());
				diagram.setName("default");
				diagramPresentations.store(diagram);
			}
		}
		
		// 初回のバリデータ起動
		commandExecuted(null);
		
		viewer.setContents(context);
		
		logger.debug(LogMarker.LIFECYCLE, "GraphicalViewer initialized");
	}
	
	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		
		// タブにファイル名をセット
		setPartName(input.getName());
	}
	
	private void configureSimpleDialect() {
		JmMetadata metadata = context.getMetadata();
		if (metadata instanceof SimpleJmMetadata) {
			((SimpleJmMetadata) metadata).setDialectClassName(GenericDialect.class.getName());
		}
		context.setMetadata(metadata);
	}
	
//	/**
//	 * アウトラインビューのページクラス。
//	 * 
//	 * @author daisuke
//	 */
//	private class DiagramOutlinePage extends org.eclipse.gef.ui.parts.ContentOutlinePage {
//		
//		/** ページをアウトラインとサムネイルに分離するコンポーネント */
//		private SashForm sash;
//		
//		/** サムネイル */
//		private Canvas overview;
//		
//		/** サムネイルを表示する為のフィギュア */
//		private ScrollableThumbnail thumbnail;
//		
//		private DisposeListener disposeListener;
//		
//		private final EditPartViewer viewer;
//		
//
//		/**
//		 * インスタンスを生成する。
//		 * 
//		 * @param viewer ビューア
//		 */
//		public DiagramOutlinePage(EditPartViewer viewer) {
//			super(viewer);
//			this.viewer = viewer;
//		}
//		
//		@Override
//		public void createControl(Composite parent) {
//			sash = new SashForm(parent, SWT.VERTICAL);
//			
//			// sash上にコンストラクタで指定したビューワの作成
//			viewer.createControl(sash);
//			
//			configureOutlineViewer();
//			hookOutlineViewer();
//			initializeOutlineViewer();
//			
//			// sash上にサムネイル用のCanvasビューワの作成
//			overview = new Canvas(sash, SWT.BORDER);
//			// サムネイル・フィギュアを配置する為の LightweightSystem
//			LightweightSystem lws = new LightweightSystem(overview);
//			
//			// RootEditPartのビューをソースとしてサムネイルを作成
//			ScalableRootEditPart rep = (ScalableRootEditPart) getGraphicalViewer().getRootEditPart();
//			thumbnail = new ScrollableThumbnail((Viewport) rep.getFigure());
//			thumbnail.setSource(rep.getLayer(LayerConstants.PRINTABLE_LAYERS));
//			
//			lws.setContents(thumbnail);
//			
//			disposeListener = new DisposeListener() {
//				
//				public void widgetDisposed(DisposeEvent e) {
//					// サムネイル・イメージの破棄
//					if (thumbnail != null) {
//						thumbnail.deactivate();
//						thumbnail = null;
//					}
//				}
//			};
//			// グラフィカル・ビューワが破棄されるときにサムネイルも破棄する
//			getGraphicalViewer().getControl().addDisposeListener(disposeListener);
//		}
//		
//		@Override
//		public void dispose() {
//			SelectionSynchronizer selectionSynchronizer = getSelectionSynchronizer();
//			// SelectionSynchronizer からTreeViewerを削除
//			selectionSynchronizer.removeViewer(viewer);
//			
//			Control control = getGraphicalViewer().getControl();
//			if (control != null && control.isDisposed() == false) {
//				control.removeDisposeListener(disposeListener);
//			}
//			
//			super.dispose();
//		}
//		
//		@Override
//		public Control getControl() {
//			return sash;
//		}
//		
//		@Override
//		public void init(IPageSite pageSite) {
//			super.init(pageSite);
//			// グラフィカル・エディタに登録されているアクションを取得
//			ActionRegistry registry = getActionRegistry();
//			// アウトライン・ページで有効にするアクション
//			IActionBars bars = pageSite.getActionBars();
//			
//			// Eclipse 3.0以前では以下のようにしてIDを取得します
//			// String id = IWorkbenchActionConstants.UNDO;
//			String id = ActionFactory.UNDO.getId();
//			bars.setGlobalActionHandler(id, registry.getAction(id));
//			
//			id = ActionFactory.REDO.getId();
//			bars.setGlobalActionHandler(id, registry.getAction(id));
//			
//			id = ActionFactory.DELETE.getId();
//			bars.setGlobalActionHandler(id, registry.getAction(id));
//			bars.updateActionBars();
//		}
//		
//		/**
//		 * ビュアーにコンテンツを設定する。
//		 * @param contents 設定するコンテンツ
//		 */
//		public void setContents(Object contents) {
//			viewer.setContents(contents);
//		}
//		
//		/**
//		 * アウトラインビュアーの設定を行う。
//		 */
//		protected void configureOutlineViewer() {
//			// エディット・ドメインの設定
//			viewer.setEditDomain(getEditDomain());
//			
//			// EditPartFactory の設定
//			viewer.setEditPartFactory(new OutlineTreeEditPartFactory());
//			
//			// THINK アウトラインに対するContextMenuの設定
//			// THINK アウトラインに対するToolBarManagerの設定
//		}
//		
//		/**
//		 * アウトラインビュアー設定用のフックメソッド。
//		 */
//		protected void hookOutlineViewer() {
//			// グラフィカル・エディタとツリー・ビューワとの間で選択を同期させる
//			SelectionSynchronizer selectionSynchronizer = getSelectionSynchronizer();
//			selectionSynchronizer.addViewer(getViewer());
//		}
//		
//		/**
//		 * アウトラインビュアーを初期化する。
//		 */
//		protected void initializeOutlineViewer() {
//			// グラフィカル・エディタのルート・モデルをツリー・ビューワにも設定
//			setContents(context);
//		}
//	}
}
