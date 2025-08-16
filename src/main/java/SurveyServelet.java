import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Main servlet handling customer satisfaction survey operations
 * Manages survey form display, submission processing, and dashboard statistics
 */
public class SurveyServlet extends HttpServlet {
    
    // Thread-safe in-memory storage for survey responses
    private static final List<SurveyResponse> surveyResponses = new CopyOnWriteArrayList<>();
    
    /**
     * Inner class representing a single survey response
     */
    public static class SurveyResponse {
        private String customerName;
        private String email;
        private int overallSatisfaction;      // 1-5 scale
        private int productQuality;           // 1-5 scale
        private int likelihoodToRecommend;    // 1-5 scale
        private int customerService;          // 1-5 scale
        private String comments;
        private Date submissionDate;
        
        // Constructor
        public SurveyResponse(String customerName, String email, int overallSatisfaction, 
                            int productQuality, int likelihoodToRecommend, int customerService, 
                            String comments) {
            this.customerName = customerName;
            this.email = email;
            this.overallSatisfaction = overallSatisfaction;
            this.productQuality = productQuality;
            this.likelihoodToRecommend = likelihoodToRecommend;
            this.customerService = customerService;
            this.comments = comments;
            this.submissionDate = new Date();
        }
        
        // Getters
        public String getCustomerName() { return customerName; }
        public String getEmail() { return email; }
        public int getOverallSatisfaction() { return overallSatisfaction; }
        public int getProductQuality() { return productQuality; }
        public int getLikelihoodToRecommend() { return likelihoodToRecommend; }
        public int getCustomerService() { return customerService; }
        public String getComments() { return comments; }
        public Date getSubmissionDate() { return submissionDate; }
    }
    
    /**
     * Handle GET requests - display survey form or dashboard
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = requestURI.substring(contextPath.length());
        
        if (path.equals("/survey")) {
            // Display survey form
            request.getRequestDispatcher("/survey.jsp").forward(request, response);
            
        } else if (path.equals("/dashboard")) {
            // Calculate statistics and display dashboard
            calculateDashboardStatistics(request);
            request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
            
        } else {
            // Default redirect to survey
            response.sendRedirect(request.getContextPath() + "/survey");
        }
    }
    
    /**
     * Handle POST requests - process survey submissions
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = requestURI.substring(contextPath.length());
        
        if (path.equals("/submit-survey")) {
            processSurveySubmission(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/survey");
        }
    }
    
    /**
     * Process survey form submission and store in memory
     */
    private void processSurveySubmission(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // Extract form parameters
            String customerName = request.getParameter("customerName");
            String email = request.getParameter("email");
            int overallSatisfaction = Integer.parseInt(request.getParameter("overallSatisfaction"));
            int productQuality = Integer.parseInt(request.getParameter("productQuality"));
            int likelihoodToRecommend = Integer.parseInt(request.getParameter("likelihoodToRecommend"));
            int customerService = Integer.parseInt(request.getParameter("customerService"));
            String comments = request.getParameter("comments");
            
            // Validate input ranges (1-5 scale)
            if (isValidRating(overallSatisfaction) && isValidRating(productQuality) && 
                isValidRating(likelihoodToRecommend) && isValidRating(customerService)) {
                
                // Create and store survey response
                SurveyResponse response_obj = new SurveyResponse(customerName, email, overallSatisfaction,
                                                               productQuality, likelihoodToRecommend, 
                                                               customerService, comments);
                surveyResponses.add(response_obj);
                
                // Set success message
                request.setAttribute("successMessage", "Thank you for your feedback! Your survey has been submitted successfully.");
                request.getRequestDispatcher("/survey.jsp").forward(request, response);
                
            } else {
                // Invalid ratings
                request.setAttribute("errorMessage", "Please provide valid ratings between 1 and 5 for all questions.");
                request.getRequestDispatcher("/survey.jsp").forward(request, response);
            }
            
        } catch (NumberFormatException e) {
            // Handle invalid number format
            request.setAttribute("errorMessage", "Please provide valid numeric ratings for all questions.");
            request.getRequestDispatcher("/survey.jsp").forward(request, response);
        } catch (Exception e) {
            // Handle any other errors
            request.setAttribute("errorMessage", "An error occurred while processing your survey. Please try again.");
            request.getRequestDispatcher("/survey.jsp").forward(request, response);
        }
    }
    
    /**
     * Validate rating is between 1 and 5
     */
    private boolean isValidRating(int rating) {
        return rating >= 1 && rating <= 5;
    }
    
    /**
     * Calculate dashboard statistics and set request attributes
     */
    private void calculateDashboardStatistics(HttpServletRequest request) {
        int totalResponses = surveyResponses.size();
        request.setAttribute("totalResponses", totalResponses);
        
        if (totalResponses == 0) {
            // No responses yet
            request.setAttribute("noData", true);
            return;
        }
        
        // Calculate averages
        double avgOverallSatisfaction = calculateAverage("overallSatisfaction");
        double avgProductQuality = calculateAverage("productQuality");
        double avgLikelihoodToRecommend = calculateAverage("likelihoodToRecommend");
        double avgCustomerService = calculateAverage("customerService");
        
        request.setAttribute("avgOverallSatisfaction", String.format("%.2f", avgOverallSatisfaction));
        request.setAttribute("avgProductQuality", String.format("%.2f", avgProductQuality));
        request.setAttribute("avgLikelihoodToRecommend", String.format("%.2f", avgLikelihoodToRecommend));
        request.setAttribute("avgCustomerService", String.format("%.2f", avgCustomerService));
        
        // Calculate percentage distributions for overall satisfaction
        Map<String, Double> satisfactionDistribution = calculateSatisfactionDistribution();
        request.setAttribute("satisfactionDistribution", satisfactionDistribution);
        
        // Calculate rating distributions for all categories
        request.setAttribute("overallDistribution", calculateRatingDistribution("overallSatisfaction"));
        request.setAttribute("productDistribution", calculateRatingDistribution("productQuality"));
        request.setAttribute("recommendDistribution", calculateRatingDistribution("likelihoodToRecommend"));
        request.setAttribute("serviceDistribution", calculateRatingDistribution("customerService"));
    }
    
    /**
     * Calculate average rating for a specific question category
     */
    private double calculateAverage(String category) {
        if (surveyResponses.isEmpty()) return 0.0;
        
        int sum = 0;
        for (SurveyResponse response : surveyResponses) {
            switch (category) {
                case "overallSatisfaction":
                    sum += response.getOverallSatisfaction();
                    break;
                case "productQuality":
                    sum += response.getProductQuality();
                    break;
                case "likelihoodToRecommend":
                    sum += response.getLikelihoodToRecommend();
                    break;
                case "customerService":
                    sum += response.getCustomerService();
                    break;
            }
        }
        return (double) sum / surveyResponses.size();
    }
    
    /**
     * Calculate satisfaction level distribution (Very Dissatisfied to Very Satisfied)
     */
    private Map<String, Double> calculateSatisfactionDistribution() {
        Map<String, Double> distribution = new LinkedHashMap<>();
        String[] levels = {"Very Dissatisfied", "Dissatisfied", "Neutral", "Satisfied", "Very Satisfied"};
        
        // Initialize all levels with 0
        for (String level : levels) {
            distribution.put(level, 0.0);
        }
        
        if (surveyResponses.isEmpty()) return distribution;
        
        // Count occurrences
        for (SurveyResponse response : surveyResponses) {
            int rating = response.getOverallSatisfaction();
            String level = levels[rating - 1]; // Convert 1-5 to 0-4 index
            distribution.put(level, distribution.get(level) + 1);
        }
        
        // Convert to percentages
        int total = surveyResponses.size();
        for (String level : levels) {
            double percentage = (distribution.get(level) / total) * 100;
            distribution.put(level, Math.round(percentage * 100.0) / 100.0); // Round to 2 decimal places
        }
        
        return distribution;
    }
    
    /**
     * Calculate rating distribution (1-5) for any category
     */
    private Map<Integer, Double> calculateRatingDistribution(String category) {
        Map<Integer, Double> distribution = new LinkedHashMap<>();
        
        // Initialize ratings 1-5 with 0
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0.0);
        }
        
        if (surveyResponses.isEmpty()) return distribution;
        
        // Count occurrences
        for (SurveyResponse response : surveyResponses) {
            int rating = 0;
            switch (category) {
                case "overallSatisfaction":
                    rating = response.getOverallSatisfaction();
                    break;
                case "productQuality":
                    rating = response.getProductQuality();
                    break;
                case "likelihoodToRecommend":
                    rating = response.getLikelihoodToRecommend();
                    break;
                case "customerService":
                    rating = response.getCustomerService();
                    break;
            }
            distribution.put(rating, distribution.get(rating) + 1);
        }
        
        // Convert to percentages
        int total = surveyResponses.size();
        for (int rating : distribution.keySet()) {
            double percentage = (distribution.get(rating) / total) * 100;
            distribution.put(rating, Math.round(percentage * 100.0) / 100.0);
        }
        
        return distribution;
    }
    
    /**
     * Get all survey responses (for potential future use)
     */
    public static List<SurveyResponse> getAllResponses() {
        return new ArrayList<>(surveyResponses);
    }
}

