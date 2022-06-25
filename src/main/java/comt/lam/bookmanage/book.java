package comt.lam.bookmanage;

import comt.lam.dataProcess;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.AfterDeleteRowEventArgs;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.control.events.RowClickEventListener;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.AfterF7SelectEvent;
import kd.bos.form.field.events.AfterF7SelectListener;
import kd.bos.form.operate.FormOperate;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Set;

public class book extends dataProcess implements RowClickEventListener, AfterF7SelectListener {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        EntryGrid entryGrid = this.getView().getControl("comt_book_entry");
        BasedataEdit basedataEdit = this.getView().getControl("comt_bookname");
        entryGrid.addRowClickListener(this);
        this.addItemClickListeners("bar_save", "bar_submitandnew", "bar_submit", "bar_audit");
        basedataEdit.addAfterF7SelectListener(this);
    }

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
        FormOperate formOperate = (FormOperate) args.getSource();
        String operateKey = formOperate.getOperateKey();
        if ("quick".equals(operateKey)) {
            quickBook();
            return;
        } else if (!operateKey.equals("newsubentry")) return;
        args.setCancel(true);
        EntryGrid selectionEntry = this.getView().getControl("comt_book_entry");
        DynamicObject[] rowsData = selectionEntry.getEntryData().getDataEntitys();
        int[] selectRows = selectionEntry.getSelectRows();
        if (selectRows.length != 1) {
            this.getView().showErrorNotification("请选择一行");
            return;
        }
        Object bookname = rowsData[selectRows[0]].get("comt_bookname");
        int index = this.getModel().createNewEntryRow("comt_book_subentry");
        this.getModel().setValue("comt_subbookname", bookname, index);
    }

    @Override
    public void beforeItemClick(BeforeItemClickEvent evt) {
        super.beforeItemClick(evt);
        if ("bar_save".equals(evt.getItemKey()) || "bar_submitandnew".equals(evt.getItemKey()) || "bar_submit".equals(evt.getItemKey())) {
            updateMergeCount();
            String hasEmptyMessage = this.CheckEntry("comt_book_entry");
            if (hasEmptyMessage == null) return;
            this.getView().showErrorNotification("已新增图书信息单据体有空的值，请检查，如已并入馆藏数量");
            evt.setCancel(true);
        }
    }

    @Override
    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);
        if (!"quickbookcallback".equals(closedCallBackEvent.getActionId())) return;
        DynamicObjectCollection returnData = (DynamicObjectCollection) closedCallBackEvent.getReturnData();
        if (returnData == null) return;
        IDataModel model = this.getModel();
        model.deleteEntryData("comt_book_subentry");

        for (DynamicObject parentsRow : returnData) {
            DynamicObjectCollection subEntry = parentsRow.getDynamicObjectCollection("comt_subentry");
            Object book = parentsRow.get("comt_bookname");
            for (DynamicObject subRow : subEntry) {
                int newRowsCount = subRow.getBigDecimal("comt_stepper").intValue();
                DynamicObject detail = (DynamicObject) subRow.get("comt_detail");
                for (int i = 0; i < newRowsCount; i++) {
                    int newRowIndex = model.createNewEntryRow("comt_book_subentry");
                    DynamicObject newRow = model.getEntryRowEntity("comt_book_subentry", newRowIndex);
                    newRow.set("comt_subbookname", book);
                    newRow.set("comt_detail", detail);
                    //自动生成图书编号，格式为book-aaa-bcddddd,a为书架序号，b为书架行，c为书架列，d为书刊序号
                    String spawnNo = detail.get("name").toString();
                    int shelfOrder = Integer.parseInt(spawnNo.substring(0, 3));
                    int rowOrder = spawnNo.charAt(4) - '0';
                    int columnOrder = spawnNo.charAt(6) - '0';
                    int bookOrder = i + 1;
                    newRow.set("comt_bookno", String.format("book-%03d-%01d%01d%05d", shelfOrder, rowOrder, columnOrder, bookOrder));
                }
            }
        }
        this.getView().updateView();
        updateMergeCount();
    }

    @Override
    public void afterDeleteRow(AfterDeleteRowEventArgs e) {
        super.afterDeleteRow(e);
    }

    @Override
    public void afterF7Select(AfterF7SelectEvent afterF7SelectEvent) {

    }

    private void quickBook() {
        HashMap<String, Object> map = new HashMap<>();
        DynamicObjectCollection entryEntity = this.getModel().getEntryEntity("comt_book_entry");
        map.put("entry", entryEntity);
        FormShowParameter parameter = new FormShowParameter();
        parameter.setFormId("comt_quickbook");
        parameter.setCaption("快速分配详细位置（将会覆盖入馆书刊单据体）");
        parameter.getOpenStyle().setShowType(ShowType.Modal);
        parameter.setCloseCallBack(new CloseCallBack(this, "quickbookcallback"));
        parameter.setShowClose(true);
        parameter.setCustomParams(map);
        this.getView().showForm(parameter);
    }

    private void updateMergeCount() {
        IDataModel model = this.getModel();
        DynamicObjectCollection bookEntry = model.getEntryEntity("comt_book_entry");
        DynamicObjectCollection bookSubEntry = model.getEntryEntity("comt_book_subentry");
        HashMap<Object, Integer> applyCounts = new HashMap<>();
        HashMap<Object, Integer> mapSelectEntry = new HashMap<>();//对选择单据体的物件进行散列，get方法返回行下标

        for (DynamicObject subRow : bookSubEntry) {
            DynamicObject name = (DynamicObject) subRow.get("comt_subbookname");
            if (name != null) {
                applyCounts.merge(name.getPkValue(), 1, Integer::sum);
            }
        }

        for (DynamicObject selectionRow : bookEntry) {
            DynamicObject bookName = (DynamicObject) selectionRow.get("comt_bookname");
            Object pkValue = bookName.getPkValue();
            Integer index = mapSelectEntry.get(pkValue);
            if (index == null) {
                mapSelectEntry.put(pkValue, bookEntry.indexOf(selectionRow));
            } else {
                //TODO 修复bug，需要已新增图书信息单据体行直接的各个字段相互比较，当有两行一样时才会进入这里
                this.getView().showErrorNotification("检测到已新增图书信息单据体含有的物件不唯一，不能分配！");
            }
        }

        Set<Object> nameSet = applyCounts.keySet();

        for (Object name : nameSet) {
            Integer applyCount = applyCounts.get(name);
            Integer index = mapSelectEntry.get(name);
            if (index != null) {
                model.setValue("comt_mergecount", applyCount, index);
            }
            if (index == null) {
                this.getView().showErrorNotification("入馆书刊单据体内含有已新增图书信息单据体不存在的物件！");
                //TODO 分配场地后删除了选择单据体行，分配单据体内还有物件，需要移除那些行
            }
        }
        this.getView().updateView();
    }
}
