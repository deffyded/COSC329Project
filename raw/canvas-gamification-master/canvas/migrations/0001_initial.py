# Generated by Django 3.0.7 on 2020-08-19 03:42

import canvas.models
from django.conf import settings
from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    initial = True

    dependencies = [
        migrations.swappable_dependency(settings.AUTH_USER_MODEL),
    ]

    operations = [
        migrations.CreateModel(
            name='CanvasCourse',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('name', models.CharField(max_length=500)),
                ('url', models.URLField()),
                ('course_id', models.IntegerField()),
                ('token', models.CharField(max_length=500)),
                ('allow_registration', models.BooleanField(default=False)),
                ('visible_to_students', models.BooleanField(default=False)),
                ('start_date', models.DateTimeField(null=True)),
                ('end_date', models.DateTimeField(null=True)),
                ('verification_assignment_group_name', models.CharField(max_length=100)),
                ('verification_assignment_group_id', models.IntegerField(blank=True, null=True)),
                ('verification_assignment_name', models.CharField(max_length=100)),
                ('verification_assignment_id', models.IntegerField(blank=True, null=True)),
            ],
        ),
        migrations.CreateModel(
            name='CanvasCourseRegistration',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('canvas_user_id', models.IntegerField(blank=True, null=True)),
                ('is_verified', models.BooleanField(db_index=True, default=False)),
                ('is_blocked', models.BooleanField(db_index=True, default=False)),
                ('verification_code', models.IntegerField(default=canvas.models.random_verification_code)),
                ('verification_attempts', models.IntegerField(default=3)),
                ('course', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='canvas.CanvasCourse')),
                ('user', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to=settings.AUTH_USER_MODEL)),
            ],
        ),
    ]
