package com.predic8.plugin.membrane_client.creator;

import groovy.xml.QName;

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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.predic8.membrane.client.core.SOAPConstants;
import com.predic8.membrane.client.core.util.SOAModelUtil;
import com.predic8.plugin.membrane_client.ImageKeys;
import com.predic8.plugin.membrane_client.MembraneClientUIPlugin;
import com.predic8.plugin.membrane_client.ui.PluginUtil;
import com.predic8.schema.Attribute;
import com.predic8.schema.ComplexType;
import com.predic8.schema.Declaration;
import com.predic8.schema.Element;
import com.predic8.schema.Extension;
import com.predic8.schema.Restriction;
import com.predic8.schema.SchemaComponent;
import com.predic8.schema.SimpleType;
import com.predic8.schema.TypeDefinition;
import com.predic8.schema.creator.AbstractSchemaCreator;
import com.predic8.schema.restriction.BaseRestriction;
import com.predic8.schema.restriction.StringRestriction;
import com.predic8.schema.restriction.facet.EnumerationFacet;
import com.predic8.schema.restriction.facet.Facet;
import com.predic8.schema.restriction.facet.LengthFacet;
import com.predic8.schema.restriction.facet.MaxLengthFacet;
import com.predic8.schema.restriction.facet.MinLengthFacet;
import com.predic8.schema.restriction.facet.PatternFacet;
import com.predic8.wsdl.BindingElement;
import com.predic8.wsdl.BindingInput;
import com.predic8.wsdl.BindingOperation;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Message;
import com.predic8.wsdl.Operation;
import com.predic8.wsdl.Part;
import com.predic8.wsdl.soap11.SOAPBody;
import com.predic8.wsdl.soap11.SOAPHeader;

public class CompositeCreator extends AbstractSchemaCreator {

	private ScrolledComposite scrollComposite;

	private Definitions definitions;

	private GridLayout gridLayout;

	private Composite root;

	private final int WIDGET_HEIGHT = 12;

	private final int WIDGET_WIDTH = 120;

	private Image removeImage = MembraneClientUIPlugin.getDefault().getImageRegistry().getDescriptor(ImageKeys.IMAGE_CROSS_REMOVE).createImage();

	private Image addImage = MembraneClientUIPlugin.getDefault().getImageRegistry().getDescriptor(ImageKeys.IMAGE_ADD_ELEMENT).createImage();

	private Stack<Composite> stack = new Stack<Composite>();

	public CompositeCreator(Composite parent) {
		parent.setLayout(new FillLayout(SWT.VERTICAL));

		gridLayout = PluginUtil.createGridlayout(1, 5);

		createScrollComposite(parent);

		createRootComposite();
		
		stack.push(root);

	}

	private void createScrollComposite(Composite parent) {
		scrollComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.DOUBLE_BUFFERED);
		scrollComposite.setExpandHorizontal(true);
		scrollComposite.setExpandVertical(true);
		scrollComposite.setLayout(new GridLayout());
	}

	private void createRootComposite() {
		root = new Composite(scrollComposite, SWT.NONE);
		root.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
		root.setLayout(gridLayout);
		root.setParent(scrollComposite);
		root.setLayoutData(PluginUtil.createGridData(GridData.FILL_HORIZONTAL, GridData.FILL_VERTICAL, true, true));
	}

	public void createComposite(String portTypeName, String operationName, String bindingName) {

		stack.clear();
		stack.push(root);

		Operation operation = definitions.getOperation(operationName, portTypeName);
		BindingOperation bindingOperation = definitions.getBinding(bindingName).getOperation(operationName);

		Message msg = operation.getInput().getMessage();

		List<SOAPHeader> bodies = SOAModelUtil.getHeaderElements(bindingOperation);
		CompositeCreatorContext ctx = new CompositeCreatorContext();
		ctx.setPath("xpath:");
		for (SOAPHeader header : bodies) {
			Part part = (Part) msg.getPart(header.getPart());
			definitions.getElement(part.getElement()).create(this, ctx);
		}

		BindingInput bInput = bindingOperation.getInput();

		List<BindingElement> list = bInput.getBindingElements();

		for (BindingElement object : list) {
			if (object instanceof SOAPBody) {
				SOAPBody body = (SOAPBody) object;
				handleMsgParts(body.getMessageParts());
			} else if (object instanceof com.predic8.wsdl.soap12.SOAPBody) {
				com.predic8.wsdl.soap12.SOAPBody body = (com.predic8.wsdl.soap12.SOAPBody) object;
				handleMsgParts((List) body.getMessageParts());
			}
		}

		layoutScrollComposite();

	}

	private void layoutScrollComposite() {
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
		for (Object part : msgParts) {
			Element element = definitions.getElement(((Part) part).getElement());
			CompositeCreatorContext ctx = new CompositeCreatorContext();
			ctx.setPath("xpath:");
			element.create(this, ctx);
		}
	}

	@Override
	public void createComplexType(ComplexType cType, Object oldContext) {
		
		CompositeCreatorContext ctx = (CompositeCreatorContext) oldContext;

		try {
			CompositeCreatorContext newCtx = ctx.clone();
			newCtx.setPath(ctx.getPath() + "/" + ctx.getElement().getName());

			SchemaComponent model = (SchemaComponent) cType.getModel();

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

	}

	private void createChildComposite(CompositeCreatorContext ctx) {

		Composite composite = new Composite(stack.peek(), SWT.BORDER);
		composite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
		composite.setLayout(gridLayout);
        composite.setLayoutData(PluginUtil.createGridData(false, false));
		
		Composite header = new Composite(composite, SWT.NONE);
		header.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
		header.setLayout(PluginUtil.createGridlayout(3, 0));
		new Label(header, SWT.NONE).setText(PluginUtil.getComplexTypeCaption(ctx));

		Composite child = new Composite(composite, SWT.NONE);
		child.setLayout(gridLayout);
		child.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
		child.setLayoutData(PluginUtil.createGridData(false, false));
		child.setData(SOAPConstants.PATH, ctx.getPath());

		if ("0".equals(ctx.getElement().getMinOccurs()))
			createAddRemoveButton(header, child, true);

		if ("unbounded".equals(ctx.getElement().getMaxOccurs())) {
			createAddButton(header, child);
		}
		
		stack.push(child);
	}

	private void writeAttributes(ComplexType cType, Object ctx) {
		List<Attribute> attributes = cType.getAttributes();
		for (Attribute attribute : attributes) {
			writeInputForBuildInType(attribute, ctx, null);
		}
	}

	@Override
	public void createElement(Element element, Object ctx) {

		if (element.getEmbeddedType() != null) {
			try {
				CompositeCreatorContext newCtx = ((CompositeCreatorContext) ctx).clone();
				newCtx.setElement(element);

				((TypeDefinition) element.getEmbeddedType()).create(this, newCtx);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			return;
		}

		TypeDefinition refType = element.getSchema().getType(element.getType());

		if (refType != null) {
			try {
				CompositeCreatorContext newCtx = ((CompositeCreatorContext) ctx).clone();
				newCtx.setElement(element);
				refType.create(this, newCtx);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			return;
		}

		writeInputForBuildInType(element, ctx, null);
	}

	private void writeInputForBuildInType(Declaration item, Object ctx, BaseRestriction restr) {

		Composite descendent = createDescendent();

		createLabel(item.getName().toString(), descendent);

		Control control = createControl(descendent, getBuildInTypeName(item), restr);

		if (control != null) {
			control.setData(SOAPConstants.PATH, ((CompositeCreatorContext) ctx).getPath() + "/" + getItemName(item));
			createAddRemoveButton(descendent, control, false);
		}

	}

	private Composite createDescendent() {
		Composite descendent = new Composite(stack.peek(), SWT.NONE);
		descendent.setLayout(PluginUtil.createGridlayout(3, 5));
		descendent.setLayoutData(PluginUtil.createGridData(false, false));
		return descendent;
	}

	private String getBuildInTypeName(Declaration item) {
		if (item.getType() != null)
			return item.getType().getLocalPart();

		if (item instanceof Element) {
			Element element = (Element) item;
			if (element.getEmbeddedType() instanceof SimpleType) {
				BaseRestriction restriction = (BaseRestriction) ((SimpleType) element.getEmbeddedType()).getRestriction();
				QName qname = (QName) restriction.getBase();
				return qname.getLocalPart();
			}
		}

		throw new RuntimeException("Can not get build in type name for item: " + item);
	}

	private Control createControl(Composite descendent, String localPart, BaseRestriction restriction) {
		if ("string".equals(localPart)) {
			if (restriction != null) {
				
			}
			return PluginUtil.createText(descendent, WIDGET_WIDTH, WIDGET_HEIGHT);
		} else if ("boolean".equals(localPart)) {
			return PluginUtil.createCheckButton(descendent, 12, 12);
		} else if ("int".equals(localPart)) {
			return PluginUtil.createText(descendent, WIDGET_WIDTH, WIDGET_HEIGHT);
		} else if ("dateTime".equals(localPart)) {
			return PluginUtil.createText(descendent, WIDGET_WIDTH, WIDGET_HEIGHT);
		}

		System.err.println("Type is not supported yet: " + localPart);

		return null;
	}

	private void createLabel(String text, Composite descendent) {
		GridData gd = new GridData();
		gd.widthHint = WIDGET_WIDTH;
		gd.heightHint = WIDGET_HEIGHT;
		Label label = new Label(descendent, SWT.NONE);
		label.setLayoutData(gd);
		label.setText(text);
	}

	private String getItemName(Declaration item) {
		return getItemName(item.getName().toString(), (item instanceof Attribute));
	}

	private String getItemName(String str, boolean attr) {
		return attr ? ("@" + str) : str;
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
				updateButtonControlEnable(control, (Button) e.getSource(), visible);
			}
		});
	}

	private void createAddButton(Composite parent, final Composite child) {
		Button bt = new Button(parent, SWT.PUSH);
		bt.setImage(addImage);
		GridData gdBt = new GridData();
		gdBt.widthHint = 10;
		gdBt.heightHint = 10;
		gdBt.horizontalIndent = 30;
		bt.setLayoutData(gdBt);
		bt.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button b = (Button)e.getSource();
				cloneAndAddChildComposite(b.getParent().getParent(), child); 
			}
		});
	}
	
	private void cloneAndAddChildComposite(Composite parent, Composite child) {
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(child.getLayout());
		composite.setBackground(child.getBackground());
		composite.setLayoutData(child.getLayoutData());
		
		Control[] children = child.getChildren();
		for (Control control : children) {
			PluginUtil.cloneControl(control, composite);
		}
		
		parent.layout();
		parent.redraw();
		
		layoutScrollComposite();
	    
	}
	
	@Override
	public void createEnumerationFacet(EnumerationFacet facet, Object context) {

		List<String> values = facet.getValues();

		Composite descendent = createDescendent();

		CompositeCreatorContext ctx = (CompositeCreatorContext) context;

		createLabel(ctx.getElement().getName().toString(), descendent);
		
		Combo combo = PluginUtil.createCombo(descendent, WIDGET_WIDTH, WIDGET_HEIGHT);
		combo.setData(SOAPConstants.PATH, ctx.getPath() + "/" + ctx.getElement().getName());
		for (String str : values) {
			combo.add(str);
		}

		if (values.size() > 0) {
			combo.select(0);
		}
		
		createAddRemoveButton(descendent, combo, false);
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

		if (control instanceof Combo) {
			Combo combo = (Combo)control;
			map.put(control.getData(SOAPConstants.PATH).toString(), combo.getItem(combo.getSelectionIndex()));
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

	@Override
	public void createExtension(Extension ext, Object ctx) {
		if (ext.getBase() != null) {
			TypeDefinition def = ext.getSchema().getType(ext.getBase());
			if (def instanceof ComplexType) {
				ComplexType type = (ComplexType) def;
				SchemaComponent model = (SchemaComponent) type.getModel();
				model.create(this, ctx);
				writeAttributes(type, ctx);
			}
		}
		
		SchemaComponent model = (SchemaComponent)ext.getModel();
		model.create(this, ctx);
		
		List<Attribute> attributes = ext.getAttributes();
		for (Attribute attribute : attributes) {
			writeInputForBuildInType(attribute, ctx, null);
		}
	}

	@Override
	public void createComplexContentRestriction(Restriction restriction, Object ctx) {
		if (restriction.getModel() != null) {
			SchemaComponent component = (SchemaComponent) restriction.getModel();
			component.create(this, ctx);
		}

		restriction.getAttributes();

		List<Attribute> attrs = restriction.getAttributes();
		for (Attribute attribute : attrs) {
			writeInputForBuildInType(attribute, ctx, null);
		}
	}

	@Override
	public void createSimpleRestriction(BaseRestriction restriction, Object ctx) {
		
		if (restriction instanceof StringRestriction) {

			StringRestriction strRest = (StringRestriction) restriction;

			List<Facet> list = strRest.getFacets();
			if (list != null && !list.isEmpty()) {
				for (Facet object : list) {
					if (object instanceof EnumerationFacet) {
						super.createSimpleRestriction(restriction, ctx);
						return;
					}
				}
			}

			TypeDefinition type = (TypeDefinition) strRest.getParent();

			if (type.getParent() instanceof Element) {
				Element element = (Element) type.getParent();

				writeInputForBuildInType(element, ctx, strRest);
			}
			return;
		}

		super.createSimpleRestriction(restriction, ctx);
	}

	@Override
	public void createLengthFacet(LengthFacet arg0, Object arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	public void createMaxLengthFacet(MaxLengthFacet arg0, Object arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	public void createMinLengthFacet(MinLengthFacet arg0, Object arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	public void createPatternFacet(PatternFacet arg0, Object arg1) {
		// TODO Auto-generated method stub
	}

}
