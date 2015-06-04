# Consumer Forward Proxy #

If your Relying Party / consumer site needs to access the internet through a proxy, the following proxy configuration is necessary, **before the ConsumerManager is instantiated**:

```
// --- Forward proxy setup (only if needed) ---
ProxyProperties proxyProps = new ProxyProperties();
proxyProps.setProxyName("proxy.example.com");
proxyProps.setProxyPort(8080);
HttpClientFactory.setProxyProperties(proxyProps);
```