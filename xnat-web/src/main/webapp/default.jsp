<%--
  ~ Site-root redirect to the Turbine homepage screen.
  ~
  ~ Turbine 5.1 (the 4.0 service-container rewrite) dropped the `template.homepage` / `screen.homepage`
  ~ auto-default: those TurbineResources.properties keys are no longer read by any Turbine class. Under
  ~ 2.3.3 they made a request to the context root (welcome-file `app` -> /app with no path-info) render
  ~ Index.vm; under 5.1 that same empty-target request reaches Turbine with a null screen template and
  ~ fails with "Couldn't map Template null to any Screen class!".
  ~
  ~ This welcome file (listed ahead of `app` in web.xml) restores the intended behavior by redirecting
  ~ the site root to the explicit homepage screen URL, which resolves normally through the pipeline.
--%><% response.sendRedirect(request.getContextPath() + "/app/template/Index.vm"); %>
