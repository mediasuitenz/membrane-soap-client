/* Copyright 2009 predic8 GmbH, www.predic8.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */

package com.predic8.plugin.membrane_client.tabcomposites;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.predic8.membrane.client.core.controller.ParamsMap;
import com.predic8.membrane.client.core.util.FormParamsExtractor;
import com.predic8.membrane.client.core.util.SOAModelUtil;
import com.predic8.membrane.core.http.Message;
import com.predic8.membrane.core.http.Request;
import com.predic8.membrane.core.http.Response;
import com.predic8.plugin.membrane_client.message.composite.MessageComposite;
import com.predic8.plugin.membrane_client.message.composite.RequestComposite;
import com.predic8.plugin.membrane_client.ui.PluginUtil;
import com.predic8.plugin.membrane_client.views.RequestView;
import com.predic8.wsdl.BindingOperation;

public class MessageTabManager {

	private Log log = LogFactory.getLog(MessageTabManager.class.getName());

	private MessageComposite baseComp;

	private TabFolder folder;

	private RawTabComposite rawTabComposite;

	private HeaderTabComposite headerTabComposite;

	private ErrorTabComposite errorTabComposite;

	private BodyTabComposite currentBodyTab;

	private SecurityTabComposite securityTabComposite;

	private List<BodyTabComposite> bodyTabs = new ArrayList<BodyTabComposite>();

	private NullBodyTabComposite nullBodyTabComposite;

	private FormTabComposite formTabComposite;

	private FormParamsExtractor extractor = new FormParamsExtractor();

	private TabItem currentSelection;

	public MessageTabManager(final MessageComposite baseComp) {
		this.baseComp = baseComp;
		folder = createTabFolder(baseComp);

		errorTabComposite = new ErrorTabComposite(folder);
		rawTabComposite = new RawTabComposite(folder);
		headerTabComposite = new HeaderTabComposite(folder);
		nullBodyTabComposite = new NullBodyTabComposite(folder);

		createFormComposite(baseComp);
		createBodyTabs();
		createSecurityTab(baseComp);

		currentBodyTab = new NullBodyTabComposite(folder);

		addSelectionListenerToFolder(baseComp);

		hideAllContentTabs();
		errorTabComposite.hide();
	}

	private void createSecurityTab(final MessageComposite baseComp) {
		if (baseComp instanceof RequestComposite) {
			securityTabComposite = new SecurityTabComposite(folder);
		}

	}

	private void createFormComposite(final MessageComposite baseComp) {
		if (baseComp instanceof RequestComposite) {
			formTabComposite = new FormTabComposite(folder);
		}
	}

	private void addSelectionListenerToFolder(final MessageComposite baseComp) {
		folder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (currentSelection != null
						&& currentSelection.equals(getCurrentBodyTabItem()))
					onBodyTabDeselected();

				currentSelection = folder.getSelection()[0];

				for (TabItem tabItem : folder.getSelection()) {
					if (tabItem.equals(rawTabComposite.getTabItem())) {
						baseComp.setFormatEnabled(false);
						baseComp.setSaveEnabled(true);
						rawTabComposite.update(baseComp.getMsg());
						break;
					} else if (tabItem.equals(headerTabComposite.getTabItem())) {
						baseComp.setFormatEnabled(false);
						baseComp.setSaveEnabled(false);
						headerTabComposite.update(baseComp.getMsg());
						break;
					} else if (tabItem.equals(getCurrentBodyTabItem())) {
						resetBodyTabContent();
						setBodyModified(false);
						baseComp.setFormatEnabled(currentBodyTab
								.isFormatSupported());
						baseComp.setSaveEnabled(currentBodyTab
								.isSaveSupported());
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				baseComp.setFormatEnabled(false);
				baseComp.setSaveEnabled(false);
			}

		});
	}

	private TabFolder createTabFolder(final MessageComposite baseComp) {
		final TabFolder folder = new TabFolder(baseComp, SWT.NONE);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		return folder;
	}

	private void createBodyTabs() {
		bodyTabs.add(new CSSTabComposite(folder));
		bodyTabs.add(new JavaScriptTabComposite(folder));
		bodyTabs.add(new HTMLTabComposite(folder));
		bodyTabs.add(new SOAPTabComposite(folder));
		bodyTabs.add(new JSONTabComposite(folder));
		bodyTabs.add(new ImageTabComposite(folder));
		bodyTabs.add(new ContentTabComposite(folder));
	}

	private TabItem getCurrentBodyTabItem() {
		return currentBodyTab.getTabItem();
	}

	public void setBodyModified(boolean b) {
		currentBodyTab.setBodyModified(b);
	}

	public void doUpdate(Message msg, BindingOperation operation, ParamsMap map) {
		if (msg == null) {
			hideAllContentTabs();
			errorTabComposite.hide();
			return;
		}

		if (msg.getErrorMessage() != null && !msg.getErrorMessage().equals("")) {
			hideAllContentTabs();
			errorTabComposite.show();
			errorTabComposite.update(msg);
			return;
		}

		if (msg.getHeader() == null) {
			hideAllBodyTabs();
			headerTabComposite.hide();
			errorTabComposite.hide();
			return;
		}

		currentBodyTab = nullBodyTabComposite;

		errorTabComposite.hide();

		rawTabComposite.show();
		rawTabComposite.update(msg);

		headerTabComposite.show();
		headerTabComposite.update(msg);

		hideAllBodyTabs();
		if (msg.getHeader().getContentType() == null || msg.getBody() == null) {
			return;
		}

		currentBodyTab = getCurrentBodyTab(msg);
		currentBodyTab.update(msg);
		currentBodyTab.show();

		currentBodyTab.setBodyModified(false);

		updateFormTabComposite(operation, msg, map);

		setSelectionForFolder();

		baseComp.setFormatEnabled(currentBodyTab.isFormatSupported());
	}

	private void setSelectionForFolder() {
		folder.setSelection(getSelectionTabItem());
		folder.notifyListeners(SWT.Selection, null);
	}

	private TabItem getSelectionTabItem() {
		if (formTabComposite != null && formTabComposite.isDisplayed())
			return formTabComposite.getTabItem();

		if (currentBodyTab != null && !currentBodyTab.isDisposed())
			return currentBodyTab.getTabItem();

		return headerTabComposite.getTabItem();
	}

	private void updateFormTabComposite(BindingOperation operation,
			Message msg, ParamsMap map) {
		if (formTabComposite == null || operation == null)
			return;

		formTabComposite.setBindingOperation(operation);
		if (msg instanceof Request) {
			try {
				if (map == null || map.getMap() == null)
					refreshFormTab(new String(msg.getBody().getContent()));
				else
					formTabComposite.setFormParams(map.getMap());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		formTabComposite.show();
		securityTabComposite.show();

	}

	private void onBodyTabDeselected() {
		if (formTabComposite == null || formTabComposite.isDisposed()
				|| !isBodyModified())
			return;

		refreshFormTab(currentBodyTab.getBodyText());
	}

	private void refreshFormTab(String xml) {
		try {
			formTabComposite.setFormParams(extractor.extract(xml));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private BodyTabComposite getCurrentBodyTab(Message msg) {
		if (msg instanceof Response) {
			if (((Response) msg).isRedirect()
					|| ((Response) msg).hasNoContent())
				return nullBodyTabComposite;
		}

		try {
			if (msg.isBodyEmpty())
				return nullBodyTabComposite;
		} catch (IOException e) {
			e.printStackTrace();
			return bodyTabs.get(2);
		}

		if (msg.isCSS()) {
			return bodyTabs.get(0);
		} else if (msg.isJavaScript()) {
			return bodyTabs.get(1);
		} else if (msg.isHTML()) {
			return bodyTabs.get(2);
		} else if (msg.isXML()) {
			return bodyTabs.get(3);
		} else if (msg.isJSON()) {
			return bodyTabs.get(4);
		} else if (msg.isImage()) {
			return bodyTabs.get(5);
		}
		return bodyTabs.get(6);
	}

	private void hideAllContentTabs() {
		rawTabComposite.hide();
		headerTabComposite.hide();

		hideAllBodyTabs();
		if (securityTabComposite != null) {
			securityTabComposite.hide();
		}
		currentBodyTab = new NullBodyTabComposite(folder);
	}

	private void hideAllBodyTabs() {
		for (BodyTabComposite bodyTab : bodyTabs) {
			bodyTab.hide();
		}
	}

	public void setMessageEditable(boolean bool) {
		currentBodyTab.setBodyTextEditable(bool);

		if (headerTabComposite != null && !headerTabComposite.isDisposed()) {
			headerTabComposite.setWidgetEditable(bool);
		}
	}

	public String getBodyText() {
		return currentBodyTab.getBodyText();
	}

	public void copyBodyFromGUIToModel() {
		try {
			baseComp.getMsg().setBodyContent(getBodyText().getBytes());
			log.debug("Body copied from GUI to model");
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	boolean openConfirmDialog(String msg) {
		return MessageDialog.openQuestion(baseComp.getShell(), "Question", msg);
	}

	public void beautify(Message msg) throws IOException {
		currentBodyTab.beautify(msg.getBody().getContent());
	}

	public boolean isBodyModified() {
		return currentBodyTab.isBodyModified();
	}

	public void setSelectionOnBodyTabItem() {
		if (!isCurrentBodyTabAvailable())
			return;
		folder.setSelection(currentBodyTab.getTabItem());
		folder.notifyListeners(SWT.Selection, null);
	}

	private boolean isCurrentBodyTabAvailable() {
		return currentBodyTab != null && !currentBodyTab.isDisposed()
				&& currentBodyTab.getTabItem() != null
				&& !currentBodyTab.getTabItem().isDisposed();
	}

	public FormTabComposite getFormTabComposite() {
		return formTabComposite;
	}

	public SecurityTabComposite getSecurityTabComposite() {
		return securityTabComposite;
	}

	public boolean isBodyTabSelected() {
		TabItem item = folder.getItem(folder.getSelectionIndex());
		if (item != null && item.equals(currentBodyTab.getTabItem()))
			return true;
		return false;
	}

	private void resetBodyTabContent() {
		if (formTabComposite == null || formTabComposite.isDisposed()) {
			currentBodyTab.update(baseComp.getMsg());
			return;
		}

		RequestView view = (RequestView) PluginUtil
				.getView(RequestView.VIEW_ID);
		String text = SOAModelUtil.getSOARequestBody(
				view.getBindingOperation(), formTabComposite.getFormParams());
		currentBodyTab.setBodyText(text);
	}
}
