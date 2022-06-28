package comt.lam.rent;

import com.alibaba.fastjson.JSONObject;
import comt.lam.dataProcess;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.form.FormShowParameter;
import kd.bos.form.events.BeforeDoOperationEventArgs;

import java.util.EventObject;
import java.util.Map;

public class lentApply extends dataProcess {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        IDataModel model = this.getModel();
        model.deleteEntryData("comt_lent_entry");
        FormShowParameter formShowParameter = this.getView().getFormShowParameter();
        Map<String, Object> params = formShowParameter.getCustomParams();
        JSONObject lent = (JSONObject) params.get("lent");
        Long pkid = (Long) params.get("pkid");
        String operate = (String) params.get("operate");
        String status;
        if ("lent".equals(operate)) {
            status = "C";
        } else if ("order".equals(operate)) {
            status = "D";
        } else {
            this.getView().showErrorNotification("未能识别到可行的图书状态！");
            return;
        }
        int index = model.createNewEntryRow("comt_lent_entry");
        model.setValue("comt_bookname", pkid, index);
        model.setValue("comt_bookno", lent.get("comt_number"), index);
        model.setValue("comt_status", status, index);
        this.getView().updateView("comt_lent_entry");
    }

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
    }
}
