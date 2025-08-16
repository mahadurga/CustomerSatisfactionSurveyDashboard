<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Map" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Survey Dashboard</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
    <div class="container">
        <header class="header">
            <h1>Survey Dashboard</h1>
            <nav class="navigation">
                <a href="<%= request.getContextPath() %>/" class="nav-link">Home</a>
                <a href="survey" class="nav-link">Take Survey</a>
            </nav>
        </header>
        
        <main class="main-content">
            <div class="dashboard-container">
                <% if (request.getAttribute("noData") != null && (Boolean)request.getAttribute("noData")) { %>
                    <!-- No Data State -->
                    <div class="no-data-state">
                        <h2>No Survey Data Available</h2>
                        <p>No survey responses have been submitted yet. Be the first to share your feedback!</p>
                        <a href="survey" class="btn btn-primary">Take the Survey</a>
                    </div>
                <% } else { %>
                    <!-- Dashboard with Data -->
                    <div class="dashboard-header">
                        <h2>Survey Results Overview</h2>
                        <div class="total-responses">
                            <span class="metric-value"><%= request.getAttribute("totalResponses") %></span>
                            <span class="metric-label">Total Responses</span>
                        </div>
                    </div>
                    
                    <!-- Average Ratings Section -->
                    <div class="dashboard-section">
                        <h3>Average Ratings</h3>
                        <div class="metrics-grid">
                            <div class="metric-card">
                                <div class="metric-header">
                                    <h4>Overall Satisfaction</h4>
                                    <span class="metric-value"><%= request.getAttribute("avgOverallSatisfaction") %></span>
                                </div>
                                <div class="rating-bar">
                                    <div class="rating-fill" style="width: <%= Double.parseDouble((String)request.getAttribute("avgOverallSatisfaction")) * 20 %>%"></div>
                                </div>
                                <span class="rating-scale">out of 5.00</span>
                            </div>
                            
                            <div class="metric-card">
                                <div class="metric-header">
                                    <h4>Product Quality</h4>
                                    <span class="metric-value"><%= request.getAttribute("avgProductQuality") %></span>
                                </div>
                                <div class="rating-bar">
                                    <div class="rating-fill" style="width: <%= Double.parseDouble((String)request.getAttribute("avgProductQuality")) * 20 %>%"></div>
                                </div>
                                <span class="rating-scale">out of 5.00</span>
                            </div>
                            
                            <div class="metric-card">
                                <div class="metric-header">
                                    <h4>Likelihood to Recommend</h4>
                                    <span class="metric-value"><%= request.getAttribute("avgLikelihoodToRecommend") %></span>
                                </div>
                                <div class="rating-bar">
                                    <div class="rating-fill" style="width: <%= Double.parseDouble((String)request.getAttribute("avgLikelihoodToRecommend")) * 20 %>%"></div>
                                </div>
                                <span class="rating-scale">out of 5.00</span>
                            </div>
                            
                            <div class="metric-card">
                                <div class="metric-header">
                                    <h4>Customer Service</h4>
                                    <span class="metric-value"><%= request.getAttribute("avgCustomerService") %></span>
                                </div>
                                <div class="rating-bar">
                                    <div class="rating-fill" style="width: <%= Double.parseDouble((String)request.getAttribute("avgCustomerService")) * 20 %>%"></div>
                                </div>
                                <span class="rating-scale">out of 5.00</span>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Satisfaction Distribution -->
                    <div class="dashboard-section">
                        <h3>Overall Satisfaction Distribution</h3>
                        <div class="distribution-chart">
                            <%
                                Map<String, Double> satisfactionDist = (Map<String, Double>) request.getAttribute("satisfactionDistribution");
                                String[] levels = {"Very Dissatisfied", "Dissatisfied", "Neutral", "Satisfied", "Very Satisfied"};
                                String[] colors = {"#ff4444", "#ff8800", "#ffbb33", "#00C851", "#007E33"};
                                
                                for (int i = 0; i < levels.length; i++) {
                                    String level = levels[i];
                                    Double percentage = satisfactionDist.get(level);
                                    String color = colors[i];
                            %>
                                <div class="distribution-item">
                                    <div class="distribution-label">
                                        <span class="level-name"><%= level %></span>
                                        <span class="percentage"><%= percentage %>%</span>
                                    </div>
                                    <div class="distribution-bar">
                                        <div class="distribution-fill" style="width: <%= percentage %>%; background-color: <%= color %>;"></div>
                                    </div>
                                </div>
                            <% } %>
                        </div>
                    </div>
                    
                    <!-- Detailed Rating Distributions -->
                    <div class="dashboard-section">
                        <h3>Rating Distributions by Category</h3>
                        <div class="rating-distributions">
                            <%
                                String[] categories = {"Overall Satisfaction", "Product Quality", "Likelihood to Recommend", "Customer Service"};
                                String[] attributeNames = {"overallDistribution", "productDistribution", "recommendDistribution", "serviceDistribution"};
                                
                                for (int i = 0; i < categories.length; i++) {
                                    String category = categories[i];
                                    Map<Integer, Double> distribution = (Map<Integer, Double>) request.getAttribute(attributeNames[i]);
                            %>
                                <div class="rating-distribution-card">
                                    <h4><%= category %></h4>
                                    <div class="rating-breakdown">
                                        <% for (int rating = 1; rating <= 5; rating++) {
                                            Double percentage = distribution.get(rating);
                                        %>
                                            <div class="rating-breakdown-item">
                                                <span class="rating-number"><%= rating %></span>
                                                <div class="breakdown-bar">
                                                    <div class="breakdown-fill" style="width: <%= percentage %>%"></div>
                                                </div>
                                                <span class="breakdown-percentage"><%= percentage %>%</span>
                                            </div>
                                        <% } %>
                                    </div>
                                </div>
                            <% } %>
                        </div>
                    </div>
                <% } %>
            </div>
        </main>
        
        <footer class="footer">
        </footer>
    </div>
</body>
</html>
