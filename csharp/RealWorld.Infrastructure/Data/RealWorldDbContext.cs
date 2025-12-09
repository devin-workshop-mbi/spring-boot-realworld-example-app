using Microsoft.EntityFrameworkCore;
using RealWorld.Domain.Entities;

namespace RealWorld.Infrastructure.Data;

public class RealWorldDbContext : DbContext
{
    public RealWorldDbContext(DbContextOptions<RealWorldDbContext> options) : base(options)
    {
    }

    public DbSet<User> Users => Set<User>();
    public DbSet<Article> Articles => Set<Article>();
    public DbSet<Tag> Tags => Set<Tag>();
    public DbSet<ArticleTag> ArticleTags => Set<ArticleTag>();
    public DbSet<Comment> Comments => Set<Comment>();
    public DbSet<FollowRelation> FollowRelations => Set<FollowRelation>();
    public DbSet<ArticleFavorite> ArticleFavorites => Set<ArticleFavorite>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        modelBuilder.Entity<User>(entity =>
        {
            entity.ToTable("users");
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.Email).HasColumnName("email");
            entity.Property(e => e.Username).HasColumnName("username");
            entity.Property(e => e.Password).HasColumnName("password");
            entity.Property(e => e.Bio).HasColumnName("bio");
            entity.Property(e => e.Image).HasColumnName("image");
        });

        modelBuilder.Entity<Article>(entity =>
        {
            entity.ToTable("articles");
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.UserId).HasColumnName("user_id");
            entity.Property(e => e.Slug).HasColumnName("slug");
            entity.Property(e => e.Title).HasColumnName("title");
            entity.Property(e => e.Description).HasColumnName("description");
            entity.Property(e => e.Body).HasColumnName("body");
            entity.Property(e => e.CreatedAt).HasColumnName("created_at");
            entity.Property(e => e.UpdatedAt).HasColumnName("updated_at");

            entity.HasOne(e => e.Author)
                .WithMany(u => u.Articles)
                .HasForeignKey(e => e.UserId);
        });

        modelBuilder.Entity<Tag>(entity =>
        {
            entity.ToTable("tags");
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.Name).HasColumnName("name");
        });

        modelBuilder.Entity<ArticleTag>(entity =>
        {
            entity.ToTable("article_tags");
            entity.HasKey(e => new { e.ArticleId, e.TagId });
            entity.Property(e => e.ArticleId).HasColumnName("article_id");
            entity.Property(e => e.TagId).HasColumnName("tag_id");

            entity.HasOne(e => e.Article)
                .WithMany(a => a.ArticleTags)
                .HasForeignKey(e => e.ArticleId);

            entity.HasOne(e => e.Tag)
                .WithMany(t => t.ArticleTags)
                .HasForeignKey(e => e.TagId);
        });

        modelBuilder.Entity<Comment>(entity =>
        {
            entity.ToTable("comments");
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.Body).HasColumnName("body");
            entity.Property(e => e.UserId).HasColumnName("user_id");
            entity.Property(e => e.ArticleId).HasColumnName("article_id");
            entity.Property(e => e.CreatedAt).HasColumnName("created_at");

            entity.HasOne(e => e.Author)
                .WithMany(u => u.Comments)
                .HasForeignKey(e => e.UserId);

            entity.HasOne(e => e.Article)
                .WithMany(a => a.Comments)
                .HasForeignKey(e => e.ArticleId);
        });

        modelBuilder.Entity<FollowRelation>(entity =>
        {
            entity.ToTable("follows");
            entity.HasKey(e => new { e.UserId, e.TargetId });
            entity.Property(e => e.UserId).HasColumnName("user_id");
            entity.Property(e => e.TargetId).HasColumnName("follow_id");

            entity.HasOne(e => e.Follower)
                .WithMany(u => u.Following)
                .HasForeignKey(e => e.UserId)
                .OnDelete(DeleteBehavior.Restrict);

            entity.HasOne(e => e.Following)
                .WithMany(u => u.Followers)
                .HasForeignKey(e => e.TargetId)
                .OnDelete(DeleteBehavior.Restrict);
        });

        modelBuilder.Entity<ArticleFavorite>(entity =>
        {
            entity.ToTable("article_favorites");
            entity.HasKey(e => new { e.ArticleId, e.UserId });
            entity.Property(e => e.ArticleId).HasColumnName("article_id");
            entity.Property(e => e.UserId).HasColumnName("user_id");

            entity.HasOne(e => e.Article)
                .WithMany(a => a.Favorites)
                .HasForeignKey(e => e.ArticleId);

            entity.HasOne(e => e.User)
                .WithMany(u => u.Favorites)
                .HasForeignKey(e => e.UserId);
        });
    }
}
