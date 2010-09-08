package com.predic8.plugin.membrane_client.creator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.predic8.membrane.client.core.SOAPConstants;
import com.predic8.plugin.membrane_client.ImageKeys;
import com.predic8.plugin.membrane_client.MembraneClientUIPlugin;
import com.predic8.plugin.membrane_client.ui.PluginUtil;
import com.predic8.schema.Attribute;
import com.predic8.schema.ComplexType;
import com.predic8.schema.Declaration;
import com.predic8.schema.Element;
import com.predic8.schema.ModelGroup;
import com.predic8.schema.Schema;
import com.predic8.schema.TypeDefinition;
import com.predic8.schema.creator.AbstractSchemaCreator;
import com.predic8.schema.restriction.facet.EnumerationFacet;
import com.predic8.wsdl.Binding;
import com.predic8.wsdl.BindingElement;
import com.predic8.wsdl.BindingInput;
import com.predic8.wsdl.BindingOperation;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Input;
import com.predic8.wsdl.Message;
import com.predic8.wsdl.Operation;
import com.predic8.wsdl.Part;
import com.predic8.wsdl.soap11.SOAPBody;

public class CompositeCreator extends AbstractSchemaCreator {

	private ScrolledComposite scrollComposite;

	private Definitions definitions;

	private GridData gridData;

	private GridLayout gridLayout;

	private Composite root;

	private final int WIDGET_HEIGHT = 12;
	
	private final int WIDGET_WIDTH = 120;
	
	private Image removeImage = MembraneClientUIPlugin.getDefault().getImageRegistry().getDescriptor(ImageKeys.IMAGE_CROSS_REMOVE).createImage();
	
	private Image addImage = MembraneClientUIPlugin.getDefault().getImageRegistry().getDescriptor(ImageKeys.IMAGE_ADD_ELEMENT).createImage();
	
	private Stack<Composite> stack = new Stack<Composite>();
	
	public CompositeCreator(Composite parent) {
		parent.setLayout(new FillLayout(SWT.VERTICAL));

		gridData = PluginUtil.createGridDataVertical();
		gridLayout = PluginUtil.createGridlayout(1, 5);
		
		scrollComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.DOUBLE_BUFFERED);
		scrollComposite.setExpandHorizontal(true);
		scrollComposite.setExpandVertical(true);
		scrollComposite.setLayout(new GridLayout());
		
		root = new Composite(scrollComposite, SWT.NONE);
		root.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
		root.setLayout(gridLayout);
		
		root.setParent(scrollComposite);

		root.setLayoutData(PluginUtil.createGridDataBoth());
		stack.push(root);

	}

	public void createComposite(String portTypeName, String operationName, String bindingName) {

		stack.clear();
		stack.push(root);
		
		Operation operation = (Operation) definitions.getOperation(operationName, portTypeName);
		BindingOperation bindingOperation = (BindingOperation) ((Binding) definitions.getBinding(bindingName)).getOperation(operationName);

		Input input = (Input) operation.getInput();

		@SuppressWarnings("unused")
		Message msg = (Message) input.getMessage();

		BindingInput bInput = (BindingInput) bindingOperation.getInput();

		List<BindingElement> list = bInput.getBindingElements();

		for (Object object : list) {
			if (object instanceof SOAPBody) {
				SOAPBody body = (SOAPBody) object;
				handleMsgParts((List) body.getMessageParts());
			} else if (object instanceof com.predic8.wsdl.soap12.SOAPBody) {
				com.predic8.wsdl.soap12.SOAPBody body = (com.predic8.wsdl.soap12.SOAPBody) object;
				handleMsgParts((List) body.getMessageParts());
			}
		}

		root.layout();
		Point point = root.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		scrollComposite.setMinSize(point);
		root.setSize(point);
		
		scrollComposite.setContent(root);

		scrollComposite.layout();
		root.layout();
		
	}

	
	@SuppressWarnings("rawtypes")
	private void handleMsgParts(List msgParts) {
		for (Object object2 : msgParts) {
			Part part = (Part) object2;

			Element element = definitions.getElement(part.getElement());
			CompositeCreatorContext ctx = new CompositeCreatorContext();
			ctx.setPath("xpath:");
			element.create(this, ctx);
		}
	}

	@Override
	public Object createComplexType(Object object, Object oldContext) {
		ComplexType cType = (ComplexType) object;
		CompositeCreatorContext ctx = (CompositeCreatorContext) oldContext;

		try {
			CompositeCreatorContext newCtx = ctx.clone();
			newCtx.setPath(ctx.getPath() + "/" + ctx.getElement().getName());

			ModelGroup model = (ModelGroup) cType.getModel();

			if (cType.getQname() != null) {
				
				createChildComposite(ctx);
				
				writeAttributes(cType, newCtx);
				if (model != null) {
					model.create(this, newCtx);
				}
			} else {
				writeAttributes(cType, newCtx);
				if (model != null) {
					model.create(this, newCtx);
				}
			}
			
			stack.pop();
			
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		return null;
	}

	private void createChildComposite(CompositeCreatorContext ctx) {

		Composite composite = new Composite(stack.peek(), SWT.BORDER);
		composite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
		composite.setLayout(gridLayout);
		
		
		Composite header = new Composite(composite, SWT.NONE);
		header.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
		header.setLayout(PluginUtil.createGridlayout(2, 0));
		new Label(header, SWT.NONE).setText(PluginUtil.getComplexTypeCaption(ctx));

		Composite child = new Composite(composite, SWT.NONE);
		child.setLayout(gridLayout);
		child.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
		child.setLayoutData(gridData);
		child.setData(SOAPConstants.PATH, ctx.getPath());
		
		if ("0".equals(ctx.getElement().getMinOccurs()))
			createAddRemoveButton(header, child, true);
		
		stack.push(child);
	}

	private void writeAttributes(ComplexType cType, Object ctx) {
		List<Attribute> attributes = cType.getAttributes();
		for (Attribute attribute : attributes) {
			writeInputForBuildInType(attribute, ctx);
		}
	}

	@Override
	public Object createElement(Object object, Object ctx) {

		Element element = (Element) object;

		if (element.getEmbeddedType() != null) {
			try {
				CompositeCreatorContext newCtx = ((CompositeCreatorContext) ctx).clone();
				newCtx.setElement(element);

				((TypeDefinition) element.getEmbeddedType()).create(this, newCtx);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			return null;
		}

		Schema schema = (Schema) element.getSchema();
		TypeDefinition refType = (TypeDefinition) schema.getType(element.getType());

		if (refType != null) {
			try {
				CompositeCreatorContext newCtx = ((CompositeCreatorContext) ctx).clone();
				newCtx.setElement(element);
				refType.create(this, newCtx);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			return null;
		}

		writeInputForBuildInType(element, ctx);

		return null;
	}

	private void writeInputForBuildInType(Declaration item, Object ctx) {
		if (item.getType() == null || !item.getType().getNamespaceURI().equals(SOAPConstants.SCHEMA_NS))
			return;

		Composite descendent = new Composite(stack.peek(), SWT.NONE);
		descendent.setLayout(PluginUtil.createGridlayout(3, 5));
		descendent.setLayoutData(gridData);

		GridData gd = new GridData();
		gd.widthHint = WIDGET_WIDTH;
		gd.heightHint = WIDGET_HEIGHT;

		String path = ((CompositeCreatorContext) ctx).getPath();

		String name = (item instanceof Attribute) ? ("@" + item.getName().toString()) : item.getName().toString();
		
		
		String localPart = item.getType().getLocalPart();
		Label label = new Label(descendent, SWT.NONE);
		label.setLayoutData(gd);
		label.setText(item.getName().toString());
		
		Control control = null;
		
		if ("string".equals(localPart)) {
			control = PluginUtil.createText(descendent, WIDGET_WIDTH, WIDGET_HEIGHT);
		} else if ("boolean".equals(localPart)) {
			control = PluginUtil.createCheckButton(descendent, 12, 12);
		} else if ("int".equals(localPart)) {
			control = PluginUtil.createText(descendent, WIDGET_WIDTH, WIDGET_HEIGHT);
		} else if ("dateTime".equals(localPart)) {
			control = PluginUtil.createText(descendent, WIDGET_WIDTH, WIDGET_HEIGHT);
		} else {
			System.out.println("local part is: " + localPart);
		}
		
		if (control != null) {
			control.setData(SOAPConstants.PATH, path + "/" + name);	
			createAddRemoveButton(descendent, control, false);
		} 
	
	}

	private void createAddRemoveButton(Composite descendent, final Control control, final boolean visible) {
		Button bt = new Button(descendent, SWT.PUSH);
		bt.setImage(removeImage);
		GridData gdBt = new GridData();
		gdBt.widthHint = 10;
		gdBt.heightHint = 10;
		gdBt.horizontalIndent = 30;
		bt.setLayoutData(gdBt);
		bt.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtonControlEnable(control, (Button)e.getSource(), visible);	
			}
		});
	}

	@Override
	public Object createEnumerationFacet(Object object, Object ctx) {
		
		try {
			EnumerationFacet facet = (EnumerationFacet)object; 
			ArrayList list = (ArrayList)facet.getValues();
			
			Composite descendent = new Composite(stack.peek(), SWT.NONE);
			descendent.setLayout(PluginUtil.createGridlayout(3, 5));
			descendent.setLayoutData(gridData);

			GridData gd = new GridData();
			gd.widthHint = WIDGET_WIDTH;
			gd.heightHint = WIDGET_HEIGHT;
			
			for (Object object2 : list) {
				Label label = new Label(descendent, SWT.NONE);
				label.setLayoutData(gd);
				label.setText(object2.toString());
				
				Control control = PluginUtil.createText(descendent, WIDGET_WIDTH, WIDGET_HEIGHT);
				createAddRemoveButton(descendent, control, false);
			}
			
			CompositeCreatorContext newCtx = ((CompositeCreatorContext) ctx).clone();
			
			
//			facet.create(this, newCtx);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		System.out.println(object.getClass().getName());
		return null;
	}

	@Override
	public Object createLengthFacet(Object arg0, Object arg1) {
		System.out.println(arg0.getClass().getName());

		return null;
	}

	@Override
	public Object createMaxLengthFacet(Object arg0, Object arg1) {
		System.out.println("Max length facet: " +  arg0.getClass().getName());
		return null;
	}

	@Override
	public Object createMinLengthFacet(Object arg0, Object arg1) {
		System.out.println("Min length facet: " + arg0.getClass().getName());
		return null;
	}

	@Override
	public Object createPatternFacet(Object arg0, Object arg1) {
		System.out.println("Pattern facet: " + arg0.getClass().getName());
		return null;
	}

	public void setDefinitions(Definitions definitions) {
		this.definitions = definitions;
	}

	public void dispose() {
		scrollComposite.dispose();
	}

	private void generateOutput(Control control, Map<String, String> map) {
		if (control instanceof Composite) {
			Control[] children = ((Composite) control).getChildren();
			for (Control child : children) {
				generateOutput(child, map);
			}
			return;
		}
		
		if (control instanceof Text) {
			map.put(control.getData(SOAPConstants.PATH).toString(), ((Text) control).getText());
			return;
		} 
		
		if (control instanceof Button) {
			map.put(control.getData(SOAPConstants.PATH).toString(), Boolean.toString(((Button) control).getSelection()));
			return;
		}
	}

	public Map<String, String> generateOutput() {
		Map<String, String> result = new HashMap<String, String>();
		generateOutput(root, result);
		return result;
	}

	private void updateButtonControlEnable(final Control control, Button source, boolean visible) {
		if (control == null)
			return;
		
		if (source.getImage().equals(removeImage)) {
			source.setImage(addImage);
			updateControl(control, false, visible);
		} else {
			source.setImage(removeImage);
			updateControl(control, true, visible);
		}
	}
 	
	private void updateControl(Control control, boolean status, boolean visible) {
		if (control == null)
			return;
		if (visible)
			control.setVisible(status);
		else
			control.setEnabled(status);
	
	}
	
}
