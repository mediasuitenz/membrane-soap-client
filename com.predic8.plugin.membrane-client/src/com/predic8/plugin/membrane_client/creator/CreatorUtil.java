package com.predic8.plugin.membrane_client.creator;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import com.predic8.membrane.client.core.SOAPConstants;
import com.predic8.membrane.client.core.SchemaConstants;
import com.predic8.plugin.membrane_client.ImageKeys;
import com.predic8.plugin.membrane_client.MembraneClientUIPlugin;
import com.predic8.plugin.membrane_client.creator.typecreators.BooleanCreator;
import com.predic8.plugin.membrane_client.creator.typecreators.DateCreator;
import com.predic8.plugin.membrane_client.creator.typecreators.DateTimeCreator;
import com.predic8.plugin.membrane_client.creator.typecreators.DecimalCreator;
import com.predic8.plugin.membrane_client.creator.typecreators.DoubleCreator;
import com.predic8.plugin.membrane_client.creator.typecreators.FloatCreator;
import com.predic8.plugin.membrane_client.creator.typecreators.IntegerCreator;
import com.predic8.plugin.membrane_client.creator.typecreators.PositiveIntegerCreator;
import com.predic8.plugin.membrane_client.creator.typecreators.StringCreator;
import com.predic8.plugin.membrane_client.creator.typecreators.StringEnumerationCreator;
import com.predic8.plugin.membrane_client.creator.typecreators.TimeCreator;
import com.predic8.plugin.membrane_client.ui.ControlUtil;
import com.predic8.plugin.membrane_client.ui.PluginUtil;
import com.predic8.schema.restriction.BaseRestriction;

public class CreatorUtil {

	public static final Image removeImage = MembraneClientUIPlugin.getDefault().getImageRegistry().getDescriptor(ImageKeys.IMAGE_CROSS_REMOVE).createImage();

	public static final Image ADD_IMAGE = MembraneClientUIPlugin.getDefault().getImageRegistry().getDescriptor(ImageKeys.IMAGE_ADD_ELEMENT).createImage();
	
	
	public static final String REGEX_POSITIVE_INT ="[0-9]?";
	
	public static final String REGEX_NON_NEGATIVE_INT ="[0-9]?";
	
	public static final String REGEX_INT = "(-)?" + REGEX_POSITIVE_INT;
	
	
	public static final String REGEX_NON_NEGATIVE_FLOAT =  "(\\d){1,10}\\.(\\d){1,10}";
	
	public static final String REGEX_FLOAT = "(-)?" + REGEX_NON_NEGATIVE_FLOAT;

	
	public static void generateOutput(Control control, Map<String, String> map) {
		if (control == null)
			return;
		
		if (control instanceof Composite) {
			Control[] children = ((Composite) control).getChildren();
			for (Control child : children) {
				generateOutput(child, map);
			}
			return;
		}

		if (control.getData(SOAPConstants.PATH) == null)
			return;
		
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
	
	public static void createControls(Composite descendent, BaseRestriction restriction, CompositeCreatorContext ctx) {
		if (SchemaConstants.SIMPLE_TYPE_STRING.equals(ctx.getTypeName())) {
			StringCreator creator = new StringCreator();
			creator.createControls(descendent, ctx, restriction);
			return;
		} 
		
		if (SchemaConstants.SIMPLE_TYPE_BOOLEAN.equals(ctx.getTypeName())) {
			BooleanCreator creator = new BooleanCreator();
			creator.createControls(descendent, ctx, restriction);
			return;
		} 
		
		if (SchemaConstants.SIMPLE_TYPE_INT.equals(ctx.getTypeName())) {
			IntegerCreator creator = new IntegerCreator();
			creator.createControls(descendent, ctx, restriction);
			return;
		} 
		
		if (SchemaConstants.SIMPLE_TYPE_INTEGER.equals(ctx.getTypeName())) {
			IntegerCreator creator = new IntegerCreator();
			creator.createControls(descendent, ctx, restriction);
			return;
		} 
		
		if (SchemaConstants.SIMPLE_TYPE_POSITIVE_INTEGER.equals(ctx.getTypeName())) {
			PositiveIntegerCreator creator = new PositiveIntegerCreator();
			creator.createControls(descendent, ctx, restriction);
			return;
		} 
		
		if (SchemaConstants.SIMPLE_TYPE_DATE_TIME.equals(ctx.getTypeName())) {
			DateTimeCreator creator = new DateTimeCreator();
			creator.createControls(descendent, ctx, restriction);
			return;
		}
		
		if (SchemaConstants.SIMPLE_TYPE_DATE.equals(ctx.getTypeName())) {
			DateCreator creator = new DateCreator();
			creator.createControls(descendent, ctx, restriction);
			return;
		}
		
		if (SchemaConstants.SIMPLE_TYPE_TIME.equals(ctx.getTypeName())) {
			TimeCreator creator = new TimeCreator();
			creator.createControls(descendent, ctx, restriction);
			return;
		}
		
		if (SchemaConstants.SIMPLE_TYPE_FLOAT.equals(ctx.getTypeName())) {
			FloatCreator creator = new FloatCreator();
			creator.createControls(descendent, ctx, restriction);
			return;
		}
		
		if (SchemaConstants.SIMPLE_TYPE_DOUBLE.equals(ctx.getTypeName())) {
			DoubleCreator creator = new DoubleCreator();
			creator.createControls(descendent, ctx, restriction);
			return;
		}
		
		
		if (SchemaConstants.SIMPLE_TYPE_DECIMAL.equals(ctx.getTypeName())) {
			DecimalCreator creator = new DecimalCreator();
			creator.createControls(descendent, ctx, restriction);
			return;
		}
				
		if (SchemaConstants.COMPLEX_TYPE_ENUMERATION.equals(ctx.getTypeName())) {
			StringEnumerationCreator creator = new StringEnumerationCreator();
			creator.createControls(descendent, ctx, restriction);
			return;
		}
		
		System.err.println("Type is not supported yet: " + ctx.getTypeName());
	}
	
	public static void updateControl(Control control, boolean status, boolean visible) {
		if (control == null)
			return;
		if (visible)
			control.setVisible(status);
		else
			control.setEnabled(status);

	}
	
	public static void updateButtonControlEnable(final Control control, Button source, boolean visible) {
		if (control == null)
			return;

		if (source.getImage().equals(removeImage)) {
			source.setImage(ADD_IMAGE);
			updateControl(control, false, visible);
		} else {
			source.setImage(removeImage);
			updateControl(control, true, visible);
		}
	}
	
	public static void createAddRemoveButton(Composite descendent, final Control control, final boolean visible) {
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
	
	public static void cloneAndAddChildComposite(Composite parent, Composite child) {
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(child.getLayout());
		composite.setBackground(child.getBackground());
		composite.setLayoutData(child.getLayoutData());
		
		Control[] children = child.getChildren();
		for (Control control : children) {
			ControlUtil.cloneControl(control, composite);
		}
		
		parent.layout();
		parent.redraw();
	}
	
	public static Button createAddButton(Composite parent) {
		Button bt = new Button(parent, SWT.PUSH);
		bt.setImage(ADD_IMAGE);
		GridData gdBt = new GridData();
		gdBt.widthHint = 10;
		gdBt.heightHint = 10;
		gdBt.horizontalIndent = 30;
		bt.setLayoutData(gdBt);
		return bt;
	}
	
	public static ScrolledComposite createScrollComposite(Composite parent) {
		ScrolledComposite sC = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.DOUBLE_BUFFERED);
		sC.setExpandHorizontal(true);
		sC.setExpandVertical(true);
		sC.setLayout(new GridLayout());
		return sC;
	}
	
	public static Composite createRootComposite(Composite parent) {
		Composite root = new Composite(parent, SWT.NONE);
		root.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
		root.setLayout(PluginUtil.createGridlayout(1, 5));
		root.setParent(parent);
		root.setLayoutData(PluginUtil.createGridData(GridData.FILL_HORIZONTAL, GridData.FILL_VERTICAL, true, true));
		return root;
	}

	public static void layoutScrolledComposites(ScrolledComposite scrollComposite, Composite root) {
		root.layout();
		Point point = root.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		scrollComposite.setMinSize(point);
		root.setSize(point);

		scrollComposite.setContent(root);

		scrollComposite.layout();
		root.layout();
	}
	
}
