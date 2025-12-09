using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using Microsoft.IdentityModel.Tokens;

namespace RealWorld.Api.Security;

public class JwtTokenFilter : IMiddleware
{
    private readonly string _secret;

    public JwtTokenFilter(IConfiguration configuration)
    {
        _secret = configuration["Jwt:Secret"] ?? throw new ArgumentNullException("Jwt:Secret configuration is required");
    }

    public async Task InvokeAsync(HttpContext context, RequestDelegate next)
    {
        var authHeader = context.Request.Headers["Authorization"].FirstOrDefault();
        
        if (!string.IsNullOrEmpty(authHeader))
        {
            var parts = authHeader.Split(' ');
            if (parts.Length == 2 && (parts[0] == "Token" || parts[0] == "Bearer"))
            {
                var token = parts[1];
                var userId = ValidateToken(token);
                
                if (userId != null)
                {
                    var claims = new[]
                    {
                        new Claim(ClaimTypes.NameIdentifier, userId),
                        new Claim(JwtRegisteredClaimNames.Sub, userId)
                    };
                    var identity = new ClaimsIdentity(claims, "jwt");
                    context.User = new ClaimsPrincipal(identity);
                }
            }
        }

        await next(context);
    }

    private string? ValidateToken(string token)
    {
        try
        {
            var tokenHandler = new JwtSecurityTokenHandler();
            var key = Encoding.UTF8.GetBytes(_secret);
            
            tokenHandler.ValidateToken(token, new TokenValidationParameters
            {
                ValidateIssuerSigningKey = true,
                IssuerSigningKey = new SymmetricSecurityKey(key),
                ValidateIssuer = false,
                ValidateAudience = false,
                ClockSkew = TimeSpan.Zero
            }, out SecurityToken validatedToken);

            var jwtToken = (JwtSecurityToken)validatedToken;
            return jwtToken.Claims.FirstOrDefault(x => x.Type == JwtRegisteredClaimNames.Sub)?.Value;
        }
        catch
        {
            return null;
        }
    }
}
