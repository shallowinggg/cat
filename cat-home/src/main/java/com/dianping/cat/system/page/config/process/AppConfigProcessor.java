package com.dianping.cat.system.page.config.process;

import org.codehaus.plexus.util.StringUtils;
import org.hsqldb.lib.StringUtil;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.config.app.AppComparisonConfigManager;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.system.config.AppRuleConfigManager;
import com.dianping.cat.system.page.config.Action;
import com.dianping.cat.system.page.config.Model;
import com.dianping.cat.system.page.config.Payload;

public class AppConfigProcessor extends BaseProcesser {

	@Inject
	private AppRuleConfigManager m_appRuleConfigManager;

	@Inject
	private AppConfigManager m_appConfigManager;

	@Inject
	private AppComparisonConfigManager m_appComparisonConfigManager;

	private void buildAppConfigInfo(AppConfigManager appConfigManager, Model model) {
		model.setConnectionTypes(appConfigManager.queryConfigItem(AppConfigManager.CONNECT_TYPE));
		model.setCities(appConfigManager.queryConfigItem(AppConfigManager.CITY));
		model.setNetworks(appConfigManager.queryConfigItem(AppConfigManager.NETWORK));
		model.setOperators(appConfigManager.queryConfigItem(AppConfigManager.OPERATOR));
		model.setPlatforms(appConfigManager.queryConfigItem(AppConfigManager.PLATFORM));
		model.setVersions(appConfigManager.queryConfigItem(AppConfigManager.VERSION));
		model.setCommands(appConfigManager.queryCommands());
	}

	public void process(Action action, Payload payload, Model model) {
		String domain, name;

		switch (action) {
		case APP_ADD:
			domain = payload.getDomain();
			name = payload.getName();
			String title = payload.getTitle();

			if (StringUtil.isEmpty(domain) || StringUtil.isEmpty(name)) {
				setUpdateResult(model, 0);
			} else {
				try {
					if (m_appConfigManager.addCommand(domain, title, name)) {
						setUpdateResult(model, 1);
					} else {
						setUpdateResult(model, 2);
					}
				} catch (Exception e) {
					setUpdateResult(model, 2);
				}
			}
			break;
		case APP_DELETE:
			domain = payload.getDomain();
			name = payload.getName();

			if (StringUtil.isEmpty(domain) || StringUtil.isEmpty(name)) {
				setUpdateResult(model, 0);
			} else {
				if (m_appConfigManager.deleteCommand(domain, name)) {
					setUpdateResult(model, 1);
				} else {
					setUpdateResult(model, 2);
				}
			}
			break;
		case APP_CONFIG_UPDATE:
			String appConfig = payload.getContent();
			if (!StringUtils.isEmpty(appConfig)) {
				model.setOpState(m_appConfigManager.insert(appConfig));
			}
			model.setContent(m_appConfigManager.getConfig().toString());
			break;
		case APP_RULE:
			buildAppConfigInfo(m_appConfigManager, model);
			model.setRules(m_appRuleConfigManager.getMonitorRules().getRules().values());
			break;
		case APP_RULE_ADD_OR_UPDATE:
			buildAppConfigInfo(m_appConfigManager, model);
			generateRuleConfigContent(payload.getRuleId(), m_appRuleConfigManager, model);
			break;
		case APP_RULE_ADD_OR_UPDATE_SUBMIT:
			buildAppConfigInfo(m_appConfigManager, model);
			model.setOpState(addSubmitRule(m_appRuleConfigManager, payload.getRuleId(), "", payload.getConfigs()));
			model.setRules(m_appRuleConfigManager.getMonitorRules().getRules().values());
			break;
		case APP_RULE_DELETE:
			buildAppConfigInfo(m_appConfigManager, model);
			model.setOpState(deleteRule(m_appRuleConfigManager, payload.getRuleId()));
			model.setRules(m_appRuleConfigManager.getMonitorRules().getRules().values());
			break;
		case APP_COMPARISON_CONFIG_UPDATE:
			String appComparisonConfig = payload.getContent();
			if (!StringUtils.isEmpty(appComparisonConfig)) {
				model.setOpState(m_appComparisonConfigManager.insert(appComparisonConfig));
			}
			model.setContent(m_appComparisonConfigManager.getConfig().toString());
			break;
		default:
			throw new RuntimeException("Error action name " + action.getName());
		}
	}

	private void setUpdateResult(Model model, int i) {
		switch (i) {
		case 0:
			model.setContent("{\"status\":500, \"info\":\"lack arguments, domain and name are required.\"}");
			break;
		case 1:
			model.setContent("{\"status\":200}");
			break;
		case 2:
			model.setContent("{\"status\":500}");
			break;
		}

	}
}
