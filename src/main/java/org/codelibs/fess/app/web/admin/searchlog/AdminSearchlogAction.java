/*
 * Copyright 2012-2018 CodeLibs Project and the Others.
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
package org.codelibs.fess.app.web.admin.searchlog;

import javax.annotation.Resource;

import org.codelibs.fess.Constants;
import org.codelibs.fess.app.pager.SearchLogPager;
import org.codelibs.fess.app.service.SearchLogService;
import org.codelibs.fess.app.web.base.FessAdminAction;
import org.codelibs.fess.util.RenderDataUtil;
import org.lastaflute.web.Execute;
import org.lastaflute.web.response.HtmlResponse;
import org.lastaflute.web.response.render.RenderData;
import org.lastaflute.web.ruts.process.ActionRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author shinsuke
 */
public class AdminSearchlogAction extends FessAdminAction {

    private static final Logger logger = LoggerFactory.getLogger(AdminSearchlogAction.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private SearchLogService searchLogService;
    @Resource
    private SearchLogPager searchLogPager;

    // ===================================================================================
    //                                                                               Hook
    //                                                                              ======
    @Override
    protected void setupHtmlData(final ActionRuntime runtime) {
        super.setupHtmlData(runtime);
        runtime.registerData("helpLink", systemHelper.getHelpLink(fessConfig.getOnlineHelpNameSearchlog()));
    }

    // ===================================================================================
    //                                                                      Search Execute
    //                                                                      ==============
    @Execute
    public HtmlResponse index() {
        saveToken();
        return asListHtml();
    }

    @Execute
    public HtmlResponse list(final Integer pageNumber, final SearchForm form) {
        saveToken();
        searchLogPager.setCurrentPageNumber(pageNumber);
        return asHtml(path_AdminSearchlog_AdminSearchlogJsp).renderWith(data -> {
            searchPaging(data, form);
        });
    }

    @Execute
    public HtmlResponse search(final SearchForm form) {
        saveToken();
        copyBeanToBean(form, searchLogPager, op -> op.exclude(Constants.PAGER_CONVERSION_RULE));
        return asHtml(path_AdminSearchlog_AdminSearchlogJsp).renderWith(data -> {
            searchPaging(data, form);
        });
    }

    @Execute
    public HtmlResponse reset(final SearchForm form) {
        saveToken();
        searchLogPager.clear();
        return asHtml(path_AdminSearchlog_AdminSearchlogJsp).renderWith(data -> {
            searchPaging(data, form);
        });
    }

    @Execute
    public HtmlResponse back(final SearchForm form) {
        saveToken();
        return asHtml(path_AdminSearchlog_AdminSearchlogJsp).renderWith(data -> {
            searchPaging(data, form);
        });
    }

    protected void searchPaging(final RenderData data, final SearchForm form) {
        RenderDataUtil.register(data, "searchLogItems", searchLogService.getSearchLogList(searchLogPager)); // page navi

        // restore from pager
        copyBeanToBean(searchLogPager, form, op -> op.include("logType"));
    }

    // ===================================================================================
    //                                                                        Edit Execute
    //                                                                        ============

    // -----------------------------------------------------
    //                                               Details
    //                                               -------
    //    @Execute
    //    public HtmlResponse details(final int crudMode, final String id) {
    //        verifyCrudMode(crudMode, CrudMode.DETAILS);
    //        saveToken();
    //        return statsService.getCrawlingInfo(id).map(entity -> {
    //            return asHtml(path_AdminCrawlinginfo_AdminCrawlinginfoDetailsJsp).useForm(EditForm.class, op -> {
    //                op.setup(form -> {
    //                    copyBeanToBean(entity, form, copyOp -> {
    //                        copyOp.excludeNull();
    //                    });
    //                    form.crudMode = crudMode;
    //                });
    //            }).renderWith(data -> {
    //                RenderDataUtil.register(data, "crawlingInfoParamItems", statsService.getCrawlingInfoParamList(id));
    //            });
    //        }).orElseGet(() -> {
    //            throwValidationError(messages -> messages.addErrorsCrudCouldNotFindCrudTable(GLOBAL, id), () -> asListHtml());
    //            return null;
    //        });
    //    }

    // -----------------------------------------------------
    //                                         Actually Crud
    //                                         -------------

    @Execute
    public HtmlResponse deleteall() {
        verifyToken(() -> asListHtml());
        searchLogPager.clear();
        saveInfo(messages -> messages.addSuccessCrawlingInfoDeleteAll(GLOBAL));
        return redirect(getClass());
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected void verifyCrudMode(final int crudMode, final int expectedMode) {
        if (crudMode != expectedMode) {
            throwValidationError(messages -> {
                messages.addErrorsCrudInvalidMode(GLOBAL, String.valueOf(expectedMode), String.valueOf(crudMode));
            }, () -> asListHtml());
        }
    }

    // ===================================================================================
    //                                                                              JSP
    //                                                                           =========

    private HtmlResponse asListHtml() {
        return asHtml(path_AdminSearchlog_AdminSearchlogJsp).renderWith(data -> {
            RenderDataUtil.register(data, "searchLogItems", searchLogService.getSearchLogList(searchLogPager)); // page navi
            }).useForm(SearchForm.class, setup -> {
            setup.setup(form -> {
                copyBeanToBean(searchLogPager, form, op -> op.include("id"));
            });
        });
    }

    private HtmlResponse asDetailsHtml() {
        return asHtml(path_AdminCrawlinginfo_AdminCrawlinginfoDetailsJsp);
    }
}