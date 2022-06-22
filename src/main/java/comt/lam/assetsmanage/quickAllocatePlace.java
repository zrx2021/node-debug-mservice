package comt.lam.assetsmanage;

import kd.bos.form.control.Control;
import kd.bos.form.plugin.AbstractFormPlugin;

import java.util.EventObject;

public class quickAllocatePlace extends AbstractFormPlugin {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        addClickListeners("btncancel", "btnok");
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Control source = (Control) evt.getSource();
        if ("btncancel".equals(source.getKey())) {
            this.getView().returnDataToParent("您点击了取消");
            this.getView().close();
        } else if ("btnok".equals(source.getKey())) {
            this.getView().returnDataToParent("您点击了确定");
            this.getView().close();
        }
    }
}
